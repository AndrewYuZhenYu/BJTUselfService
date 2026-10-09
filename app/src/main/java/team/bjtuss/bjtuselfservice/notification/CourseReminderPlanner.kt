package team.bjtuss.bjtuselfservice.notification

import team.bjtuss.bjtuselfservice.entity.CourseEntity
import team.bjtuss.bjtuselfservice.utils.parseCourseWeeks
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class CourseReminderOccurrence(
    val key: String,
    val course: CourseEntity,
    val date: LocalDate,
    val startTime: LocalTime,
    val notifyAtMillis: Long,
)

object CourseReminderPlanner {
    val periodStartTimes: List<LocalTime> = listOf(
        "08:00", "10:10", "12:10", "14:10", "16:20", "19:00", "21:00"
    ).map(LocalTime::parse)

    fun weekStart(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun isDueBeforeClass(
        event: CourseReminderOccurrence,
        nowMillis: Long,
        zoneId: ZoneId,
    ): Boolean {
        val startAt = event.date.atTime(event.startTime).atZone(zoneId).toInstant().toEpochMilli()
        return event.notifyAtMillis <= nowMillis && nowMillis < startAt
    }

    fun plan(
        courses: List<CourseEntity>,
        verifiedWeek: Int,
        verifiedAtMillis: Long,
        nowMillis: Long,
        leadMinutes: Int,
        zoneId: ZoneId,
        futureOnly: Boolean = true,
    ): List<CourseReminderOccurrence> {
        if (verifiedWeek <= 0 || verifiedAtMillis <= 0 || verifiedAtMillis > nowMillis ||
            leadMinutes !in setOf(5, 10, 15, 30)
        ) return emptyList()

        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        val verifiedDate = Instant.ofEpochMilli(verifiedAtMillis).atZone(zoneId).toLocalDate()
        if (weekStart(today) != weekStart(verifiedDate)) return emptyList()

        val monday = weekStart(today)
        return courses.asSequence()
            .filter { !it.isCurrentSemester && verifiedWeek in parseCourseWeeks(it.courseTime) }
            .mapNotNull { course ->
                val slot = course.courseLocationIndex / 8
                val weekday = course.courseLocationIndex % 8
                if (course.courseLocationIndex < 0 || slot !in periodStartTimes.indices || weekday !in 1..7) {
                    return@mapNotNull null
                }
                val date = monday.plusDays((weekday - 1).toLong())
                val startTime = periodStartTimes[slot]
                val notifyAt = date.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli() -
                    leadMinutes * 60_000L
                if (futureOnly && notifyAt <= nowMillis) return@mapNotNull null
                CourseReminderOccurrence(
                    key = "$date|${course.courseId}|${course.courseLocationIndex}|${course.courseTime}|${course.coursePlace}",
                    course = course,
                    date = date,
                    startTime = startTime,
                    notifyAtMillis = notifyAt,
                )
            }
            .distinctBy { it.key }
            .sortedBy { it.notifyAtMillis }
            .toList()
    }
}
