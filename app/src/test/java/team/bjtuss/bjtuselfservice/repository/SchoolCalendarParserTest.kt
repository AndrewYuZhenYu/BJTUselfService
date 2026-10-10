package team.bjtuss.bjtuselfservice.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder
import java.time.LocalDate

class SchoolCalendarParserTest {
    @Test
    fun keepsSchoolWeekLabelsAndDatesAcrossHolidayBreak() {
        fun week(monday: String, label: String, regularMonday: Boolean): String {
            val date = LocalDate.parse(monday)
            val timestamp = date.atStartOfDay(java.time.ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
            val keys = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val fields = keys.flatMapIndexed { index, key ->
                listOf(
                    "\"$key\":${date.plusDays(index.toLong()).dayOfMonth}",
                    "\"BB$key\":true",
                    "\"B$key\":${if (index == 0) regularMonday else true}",
                    "\"L$key\":false"
                )
            }
            return "{\"DT\":\"/Date($timestamp+0800)/\",\"Week\":\"$label\",\"Plan\":\"\",${fields.joinToString(",")}}"
        }
        val json = "[{\"Id\":49,\"Json\":[" +
            week("2026-09-28", "休", false) + "," +
            week("2026-10-05", "第4教学周", false) + "]}]"
        val encoded = URLEncoder.encode(json, "UTF-8")
        val html = """
            <select id="ddlSemTitle"><option value="49">第一学期（2026-2027学年）</option></select>
            <div id="div_49"><div id="show49"></div><div>开学：9月7日。<br>国庆节：以通知为准。</div></div>
            <input id="hidJson" value="$encoded">
        """.trimIndent()

        val semester = SchoolCalendarParser.parse(html).single()
        assertEquals("第一学期（2026-2027学年）", semester.title)
        assertEquals("休", semester.days[LocalDate.of(2026, 10, 1)]?.weekLabel)
        assertEquals("第4教学周", semester.days[LocalDate.of(2026, 10, 5)]?.weekLabel)
        assertFalse(semester.days[LocalDate.of(2026, 10, 5)]!!.regularDay)
        assertTrue(semester.days.containsKey(LocalDate.of(2026, 10, 11)))
        assertEquals(listOf("开学：9月7日。", "国庆节：以通知为准。"), semester.remarks)
    }
}
