package team.bjtuss.bjtuselfservice.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class RoomOccupancyParserTest {
    @Test
    fun parsesAllSevenDaysAndTreatsUnknownColorAsUnavailable() {
        val colors = MutableList(49) { "#fff" }
        colors[1] = "#e46868"
        colors[2] = "#9e6868"
        colors[3] = "#77bf6d"
        colors[4] = "#d8cc56"
        colors[7] = "#394ed6"
        colors[48] = "#123456"

        val week = RoomOccupancyParser.parse(table(colors), 4)

        assertEquals(4, week.week)
        assertEquals("星期一 10月05日", week.dayLabels.first())
        assertEquals("SY101", week.rooms.single().name)
        assertEquals(90, week.rooms.single().capacity)
        assertEquals(RoomOccupancyStatus.FREE, week.rooms.single().status(0, 0))
        assertEquals(RoomOccupancyStatus.SCHEDULED, week.rooms.single().status(0, 1))
        assertEquals(RoomOccupancyStatus.ADJUSTED, week.rooms.single().status(0, 2))
        assertEquals(RoomOccupancyStatus.LAB, week.rooms.single().status(0, 3))
        assertEquals(RoomOccupancyStatus.OTHER, week.rooms.single().status(0, 4))
        assertEquals(RoomOccupancyStatus.EXAM, week.rooms.single().status(1, 0))
        assertEquals(RoomOccupancyStatus.UNKNOWN, week.rooms.single().status(6, 6))
    }

    @Test
    fun extractsAvailableWeeksFromPageOptions() {
        val html = "<select><option>第4周</option><option>第 5 周</option><option>2026-2027-1</option></select>"
        assertEquals(listOf(4, 5), RoomOccupancyParser.availableWeeks(html))
    }

    @Test
    fun readsEveryBuildingLabelAndServerValueWithoutPrefixFiltering() {
        val html = """
            <select name="jxl">
              <option value="0">-- 教学楼 --</option>
              <option value="17">第十七号教学楼</option>
              <option value="sy">思源楼</option>
              <option value="xyt">新综合体育馆</option>
              <option value="yz">逸夫楼</option>
            </select>
        """.trimIndent()
        val filter = RoomOccupancyParser.buildingFilter(html)!!
        assertEquals("jxl", filter.queryKey)
        assertEquals(
            listOf("第十七号教学楼", "思源楼", "新综合体育馆", "逸夫楼"),
            filter.buildings.map { it.name },
        )
        assertEquals(listOf("17", "sy", "xyt", "yz"), filter.buildings.map { it.value })
    }

    @Test
    fun allowsBuildingWithNoRoomsButNotMissingTable() {
        val html = "<table><tr><th>星期</th>${(1..7).joinToString("") { "<th>第${it}天</th>" }}</tr><tr><th>教室/节次</th></tr></table>"
        val week = RoomOccupancyParser.parse(html, 4)
        assertEquals(emptyList<RoomOccupancy>(), week.rooms)
        assertEquals("第1天", week.dayLabels.first())
    }

    @Test
    fun buildingListIsNotLimitedToTheFormerShortList() {
        val html = buildString {
            append("<select name='jxl'><option value='17'>第十七号教学楼</option><option value='sy'>思源楼</option>")
            repeat(38) { append("<option value='b$it'>第${it}教学楼</option>") }
            append("</select>")
        }
        assertEquals(40, RoomOccupancyParser.buildingFilter(html)!!.buildings.size)
    }

    @Test
    fun missingTableDoesNotBecomeAnEmptyFreeSchedule() {
        assertThrows(IOException::class.java) { RoomOccupancyParser.parse("<html>登录失效</html>", 4) }
    }

    private fun table(colors: List<String>): String = buildString {
        append("<table><tr><th>星期</th>")
        repeat(7) { day -> append("<th>星期${"一二三四五六日"[day]} 10月05日</th>") }
        append("</tr><tr><th>教室/节次</th>")
        repeat(49) { append("<th>节次</th>") }
        append("</tr><tr><td>SY101 (90)</td>")
        colors.forEach { color -> append("<td style=\"background-color: $color\"></td>") }
        append("</tr></table>")
    }
}
