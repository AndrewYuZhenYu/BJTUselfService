package team.bjtuss.bjtuselfservice.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import team.bjtuss.bjtuselfservice.entity.CourseEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class CourseReminderPlannerTest {
    private val zone = ZoneId.of("Asia/Shanghai")
    private val monday = LocalDate.of(2026, 1, 5)

    @Test
    fun schedulesOnlyRemainingCoursesInVerifiedWeek() {
        val verifiedAt = at(monday, "07:00")
        val courses = listOf(
            course("MON", 1, "第4周"),
            course("TUE", 2, "第4周"),
            course("OTHER", 3, "第3,5周"),
            course("SELECTION", 4, "第4周", isCurrentSemester = true),
        )

        val planned = CourseReminderPlanner.plan(courses, 4, verifiedAt, at(monday, "07:01"), 10, zone)

        assertEquals(listOf("MON", "TUE"), planned.map { it.course.courseId })
        assertEquals(at(monday, "07:50"), planned.first().notifyAtMillis)
        assertEquals(at(monday.plusDays(1), "07:50"), planned.last().notifyAtMillis)
    }

    @Test
    fun keepsDifferentCoursesInTheSameSlot() {
        val courses = listOf(course("A", 10, "第4周"), course("B", 10, "第4周"))

        val planned = CourseReminderPlanner.plan(
            courses, 4, at(monday, "07:00"), at(monday, "07:01"), 15, zone
        )

        assertEquals(2, planned.size)
        assertEquals(1, planned.map { it.notifyAtMillis }.distinct().size)
        assertEquals(2, planned.map { it.key }.distinct().size)
        assertEquals(LocalTime.of(10, 10), planned.first().startTime)
    }

    @Test
    fun doesNotInferTheNextTeachingWeek() {
        val courses = listOf(course("A", 1, "第4-5周"))

        val planned = CourseReminderPlanner.plan(
            courses, 4, at(monday, "07:00"), at(monday.plusDays(7), "07:00"), 10, zone
        )

        assertTrue(planned.isEmpty())
    }

    @Test
    fun ignoresUnknownWeekFormatAndElapsedReminderTime() {
        val courses = listOf(course("UNKNOWN", 1, "第1-8周(单)"), course("PAST", 1, "第4周"))

        val planned = CourseReminderPlanner.plan(
            courses, 4, at(monday, "07:00"), at(monday, "07:55"), 10, zone
        )

        assertTrue(planned.isEmpty())
    }

    @Test
    fun simulatedSevenFiftyMatchesAnEightOClockClass() {
        val event = CourseReminderPlanner.plan(
            listOf(course("MORNING", 1, "第4周")),
            4, at(monday, "00:00"), at(monday, "07:50"), 10, zone,
            futureOnly = false,
        ).single()

        assertFalse(CourseReminderPlanner.isDueBeforeClass(event, at(monday, "07:49"), zone))
        assertTrue(CourseReminderPlanner.isDueBeforeClass(event, at(monday, "07:50"), zone))
        assertFalse(CourseReminderPlanner.isDueBeforeClass(event, at(monday, "08:00"), zone))
    }

    private fun at(date: LocalDate, time: String): Long =
        date.atTime(LocalTime.parse(time)).atZone(zone).toInstant().toEpochMilli()

    private fun course(
        id: String,
        index: Int,
        weeks: String,
        isCurrentSemester: Boolean = false,
    ) = CourseEntity(
        courseId = id,
        courseName = "课程$id",
        courseTeacher = "教师",
        courseLocationIndex = index,
        courseTime = weeks,
        coursePlace = "教室",
        isCurrentSemester = isCurrentSemester,
    )
}
