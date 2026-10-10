package team.bjtuss.bjtuselfservice.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import team.bjtuss.bjtuselfservice.StudentAccountManager
import java.io.IOException
import java.net.URLDecoder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class SchoolCalendarDay(
    val date: LocalDate,
    val inSemester: Boolean,
    val regularDay: Boolean,
    val emphasized: Boolean,
    val weekLabel: String,
    val plan: String,
)

data class SchoolCalendarSemester(
    val id: Int,
    val title: String,
    val days: Map<LocalDate, SchoolCalendarDay>,
    val remarks: List<String>,
)

object SchoolCalendarParser {
    private val dayKeys = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val datePattern = Regex("""/Date\((\d+)""")

    fun parse(html: String): List<SchoolCalendarSemester> {
        val document = Jsoup.parse(html)
        val encoded = document.getElementById("hidJson")?.attr("value")
            ?.takeIf { it.isNotBlank() } ?: throw IOException("校历页面没有日期数据")
        val calendarData = JsonParser.parseString(URLDecoder.decode(encoded, "UTF-8")).asJsonArray
            .associateBy { it.asJsonObject.get("Id").asInt }

        return document.select("#ddlSemTitle option").mapNotNull { option ->
            val id = option.attr("value").toIntOrNull() ?: return@mapNotNull null
            val weekData = calendarData[id]?.asJsonObject?.getAsJsonArray("Json") ?: return@mapNotNull null
            val days = linkedMapOf<LocalDate, SchoolCalendarDay>()
            weekData.forEach { weekElement ->
                val week = weekElement.asJsonObject
                val timestamp = datePattern.find(week.get("DT")?.asString.orEmpty())
                    ?.groupValues?.get(1)?.toLongOrNull() ?: return@forEach
                val monday = Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("Asia/Shanghai")).toLocalDate()
                val label = week.get("Week")?.asString.orEmpty().trim()
                val plan = week.get("Plan")?.asString.orEmpty().trim()
                dayKeys.forEachIndexed { index, key ->
                    val date = monday.plusDays(index.toLong())
                    if (week.get(key)?.asInt != date.dayOfMonth) {
                        throw IOException("校历日期与星期不一致：$date")
                    }
                    days[date] = SchoolCalendarDay(
                        date = date,
                        inSemester = week.get("BB$key")?.asBoolean ?: false,
                        regularDay = week.get("B$key")?.asBoolean ?: true,
                        emphasized = week.get("L$key")?.asBoolean ?: false,
                        weekLabel = label,
                        plan = plan,
                    )
                }
            }
            if (days.isEmpty()) return@mapNotNull null
            val remarkContainer = document.getElementById("div_$id")?.children()
                ?.lastOrNull { it.tagName() == "div" }
            SchoolCalendarSemester(id, option.text().trim(), days, remarkContainer?.let(::readRemarks).orEmpty())
        }.ifEmpty { throw IOException("学校尚未提供可查看的校历") }
    }

    private fun readRemarks(root: Element): List<String> {
        val text = StringBuilder()
        fun visit(node: Node) {
            when (node) {
                is TextNode -> text.append(node.text())
                is Element -> {
                    if (node.tagName() == "br") text.append('\n')
                    else {
                        node.childNodes().forEach(::visit)
                        if (node.tagName() in setOf("p", "div", "tr")) text.append('\n')
                    }
                }
            }
        }
        visit(root)
        return text.toString().lineSequence()
            .map { it.trim().replace(Regex("""\s+"""), " ") }
            .filter { it.isNotEmpty() }
            .toList()
    }
}

object SchoolCalendarRepository {
    private const val URL = "https://bksy.bjtu.edu.cn/Admin/SemesterTranPage.aspx"

    suspend fun load(): Result<List<SchoolCalendarSemester>> = runCatching {
        withContext(Dispatchers.IO) {
            StudentAccountManager.getInstance().getClient().newCall(
                Request.Builder().url(URL).build()
            ).execute().use { response ->
                if (!response.isSuccessful) throw IOException("学校校历暂时无法访问")
                SchoolCalendarParser.parse(response.body?.string() ?: throw IOException("校历页面为空"))
            }
        }
    }
}
