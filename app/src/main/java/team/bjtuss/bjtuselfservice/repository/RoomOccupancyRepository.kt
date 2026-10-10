package team.bjtuss.bjtuselfservice.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import team.bjtuss.bjtuselfservice.StudentAccountManager
import team.bjtuss.bjtuselfservice.statemanager.AppStateManager
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

enum class RoomOccupancyStatus(val label: String) {
    FREE("空闲"),
    SCHEDULED("排课"),
    ADJUSTED("调课"),
    EXAM("考试"),
    LAB("实验"),
    OTHER("其他安排"),
    UNKNOWN("状态未知"),
}

data class RoomOccupancy(
    val name: String,
    val capacity: Int?,
    val periods: List<RoomOccupancyStatus>,
) {
    fun status(day: Int, period: Int): RoomOccupancyStatus =
        periods.getOrNull(day * 7 + period) ?: RoomOccupancyStatus.UNKNOWN
}

data class RoomOccupancyWeek(
    val week: Int,
    val dayLabels: List<String>,
    val rooms: List<RoomOccupancy>,
    val currentWeek: Int = week,
    val availableWeeks: List<Int> = listOf(week),
    val buildings: List<RoomBuilding> = emptyList(),
    val selectedBuilding: String = "",
)

data class RoomBuilding(val value: String, val name: String)

data class RoomBuildingFilter(val queryKey: String, val buildings: List<RoomBuilding>)

object RoomOccupancyParser {
    private val colorPattern = Regex("""background-color\s*:\s*(#[0-9a-fA-F]{3,6})""", RegexOption.IGNORE_CASE)
    private val roomPattern = Regex("""^(.*?)\s*\((\d+)\)\s*$""")
    private val weekPattern = Regex("""第\s*(\d+)\s*周""")
    private val fallbackDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    fun availableWeeks(html: String): List<Int> = Jsoup.parse(html).select("option")
        .mapNotNull { weekPattern.find(it.text())?.groupValues?.get(1)?.toIntOrNull() }
        .distinct()
        .sorted()

    fun buildingFilter(html: String): RoomBuildingFilter? {
        val select = Jsoup.parse(html).select("select").firstOrNull { element ->
            val labels = element.select("option").map { it.text().trim() }
            labels.contains("第十七号教学楼") && labels.contains("思源楼")
        } ?: return null
        val queryKey = select.attr("name").ifBlank { select.id() }
        if (queryKey.isBlank()) return null
        val buildings = select.select("option").mapNotNull { option ->
            val value = option.attr("value").trim()
            val name = option.text().trim()
            if (value.isBlank() || name.isBlank() || name == "-- 教学楼 --") null else RoomBuilding(value, name)
        }
        return RoomBuildingFilter(queryKey, buildings).takeIf { it.buildings.isNotEmpty() }
    }

    fun dayLabels(html: String): List<String> {
        val table = Jsoup.parse(html).selectFirst("table")
            ?: throw IOException("教室占用页面没有数据表")
        val headings = table.selectFirst("tr")?.select("th, td")?.map { it.text().trim() }.orEmpty()
        return if (headings.size >= 8) headings.drop(1).take(7) else fallbackDays
    }

    fun parse(html: String, week: Int): RoomOccupancyWeek {
        val table = Jsoup.parse(html).selectFirst("table")
            ?: throw IOException("教室占用页面没有数据表")
        val rows = table.select("tr")
        if (rows.size < 2) throw IOException("教室占用数据不完整")

        val headings = rows[0].select("th, td").map { it.text().trim() }
        if (headings.size < 8) throw IOException("教室占用表头不完整")
        val dayLabels = if (headings.size >= 8) headings.drop(1).take(7) else fallbackDays
        val rooms = rows.drop(2).mapNotNull { row ->
            val cells = row.select("td")
            if (cells.size < 50) return@mapNotNull null
            val rawName = cells[0].text().trim()
            if (rawName.isEmpty()) return@mapNotNull null
            val match = roomPattern.matchEntire(rawName)
            val name = match?.groupValues?.get(1)?.trim() ?: rawName
            val capacity = match?.groupValues?.get(2)?.toIntOrNull()
            RoomOccupancy(
                name = name,
                capacity = capacity,
                periods = cells.drop(1).take(49).map { cell ->
                    when (colorPattern.find(cell.attr("style"))?.groupValues?.get(1)?.lowercase()) {
                        "#fff", "#ffffff" -> RoomOccupancyStatus.FREE
                        "#e46868" -> RoomOccupancyStatus.SCHEDULED
                        "#9e6868" -> RoomOccupancyStatus.ADJUSTED
                        "#394ed6" -> RoomOccupancyStatus.EXAM
                        "#77bf6d" -> RoomOccupancyStatus.LAB
                        "#d8cc56" -> RoomOccupancyStatus.OTHER
                        else -> RoomOccupancyStatus.UNKNOWN
                    }
                },
            )
        }
        return RoomOccupancyWeek(week, dayLabels, rooms)
    }
}

object RoomOccupancyRepository {
    const val ALL_BUILDINGS = "__all__"
    private const val PAGE_SIZE = 500
    private const val MAX_PAGES = 6
    private const val CACHE_MS = 2 * 60 * 1000L
    private const val PAGE_URL = "https://aa.bjtu.edu.cn/classroom/timeholdresult/room_view/"
    private val account = StudentAccountManager.getInstance()
    private data class Metadata(
        val url: okhttp3.HttpUrl,
        val currentWeek: Int,
        val weeks: List<Int>,
        val filter: RoomBuildingFilter,
        val currentDayLabels: List<String>,
    )
    private data class CacheKey(val week: Int, val building: String)
    private val lock = Any()
    private var metadata: Pair<Long, Metadata>? = null
    private val cache = mutableMapOf<CacheKey, Pair<Long, RoomOccupancyWeek>>()

    suspend fun load(week: Int? = null, building: String = "", forceRefresh: Boolean = false): Result<RoomOccupancyWeek> {
        return try {
            Result.success(withContext(Dispatchers.IO) {
                AppStateManager.awaitLoginState()
                val now = System.currentTimeMillis()
                val site = synchronized(lock) { metadata?.takeIf { !forceRefresh && now - it.first < CACHE_MS }?.second }
                    ?: fetchMetadata().also { synchronized(lock) { metadata = now to it; cache.clear() } }
                val selectedWeek = week ?: site.currentWeek
                if (selectedWeek !in site.weeks) throw IOException("网站未提供第${selectedWeek}周数据")
                if (building.isNotBlank() && building != ALL_BUILDINGS && site.filter.buildings.none { it.value == building }) {
                    throw IOException("网站未提供该教学楼")
                }
                val key = CacheKey(selectedWeek, building)
                synchronized(lock) { cache[key]?.takeIf { !forceRefresh && now - it.first < CACHE_MS }?.second }
                    ?: fetchWeek(site, selectedWeek, building).also { result ->
                        synchronized(lock) { cache[key] = System.currentTimeMillis() to result }
                    }
            })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchMetadata(): Metadata {
        val response = account.getClient().newCall(Request.Builder().url(PAGE_URL).build()).execute().use { result ->
            if (!result.isSuccessful) throw IOException("教室查询页面暂不可用")
            result.request.url to (result.body?.string() ?: "")
        }
        val currentWeek = response.first.queryParameter("zc")?.toIntOrNull()
            ?: throw IOException("无法确认网站当前教学周，请重新登录")
        val filter = RoomOccupancyParser.buildingFilter(response.second)
            ?: throw IOException("无法读取学校网站的教学楼列表，请稍后重试")
        val weeks = RoomOccupancyParser.availableWeeks(response.second).ifEmpty { listOf(currentWeek) }
        return Metadata(response.first, currentWeek, weeks, filter, RoomOccupancyParser.dayLabels(response.second))
    }

    private fun fetchWeek(site: Metadata, week: Int, building: String): RoomOccupancyWeek {
        if (building.isBlank() && week == site.currentWeek) {
            return RoomOccupancyWeek(
                week = week,
                dayLabels = site.currentDayLabels,
                rooms = emptyList(),
                currentWeek = site.currentWeek,
                availableWeeks = site.weeks,
                buildings = site.filter.buildings,
            )
        }
        val rooms = linkedMapOf<String, RoomOccupancy>()
        var dayLabels = emptyList<String>()
        val pageSize = if (building.isBlank()) 1 else PAGE_SIZE
        val pageLimit = if (building.isBlank()) 1 else MAX_PAGES
        for (page in 1..pageLimit) {
            val url = site.url.newBuilder()
                .setQueryParameter("zc", week.toString())
                .setQueryParameter("page", page.toString())
                .setQueryParameter("perpage", pageSize.toString())
                .apply { if (building.isNotBlank() && building != ALL_BUILDINGS) setQueryParameter(site.filter.queryKey, building) }
                .build()
            val html = account.getClient().newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful || response.request.url.queryParameter("zc")?.toIntOrNull() != week) {
                    throw IOException("网站未返回第${week}周的教室数据")
                }
                response.body?.string() ?: throw IOException("教室数据为空")
            }
            val parsed = RoomOccupancyParser.parse(html, week)
            if (page == 1) dayLabels = parsed.dayLabels
            if (building.isBlank()) break
            val previousCount = rooms.size
            parsed.rooms.forEach { rooms.putIfAbsent(it.name, it) }
            if (parsed.rooms.isNotEmpty() && rooms.size == previousCount) {
                throw IOException("网站分页未返回新的教室，无法确认数据完整")
            }
            if (parsed.rooms.size < PAGE_SIZE) break
            if (page == MAX_PAGES) throw IOException("教室数据分页未完整加载")
        }
        return RoomOccupancyWeek(
            week = week,
            dayLabels = dayLabels,
            rooms = rooms.values.toList(),
            currentWeek = site.currentWeek,
            availableWeeks = site.weeks,
            buildings = site.filter.buildings,
            selectedBuilding = building,
        )
    }
}
