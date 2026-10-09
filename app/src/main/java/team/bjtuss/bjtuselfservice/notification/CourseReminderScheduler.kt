package team.bjtuss.bjtuselfservice.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import team.bjtuss.bjtuselfservice.MainActivity
import team.bjtuss.bjtuselfservice.R
import team.bjtuss.bjtuselfservice.database.AppDatabase
import team.bjtuss.bjtuselfservice.repository.DataStoreRepository
import team.bjtuss.bjtuselfservice.utils.compactCoursePlaceText
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object CourseReminderScheduler {
    enum class TestResult {
        SENT,
        NOT_DEBUG_BUILD,
        REMINDER_DISABLED,
        NOTIFICATION_PERMISSION_REQUIRED,
        WEEK_NOT_VERIFIED,
        NO_MATCHING_COURSE,
    }

    data class TestOutcome(val result: TestResult, val count: Int = 0)

    const val OPEN_SCHEDULE_EXTRA = "open_course_schedule_from_notification"
    private const val CHANNEL_ID = "course_reminders"
    private const val ACTION_DELIVER = "team.bjtuss.bjtuselfservice.DELIVER_COURSE_REMINDER"
    private const val EXTRA_EVENT_KEY = "event_key"
    private const val EXTRA_NOTIFY_AT = "notify_at"
    private val lock = Mutex()

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "课前提醒",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "上课前显示课程名称、时间和地点" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canPostNotifications(context: Context): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java)
        return manager.areNotificationsEnabled() &&
            manager.getNotificationChannel(CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED)
    }

    fun canSchedulePrecisely(context: Context): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java)
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
    }

    suspend fun reconcile(context: Context) {
        val overdueTimes = reconcileLocked(context)
        overdueTimes.forEach { notifyAt -> deliver(context, alarmKey(notifyAt), notifyAt) }
    }

    private suspend fun reconcileLocked(context: Context): List<Long> = lock.withLock {
        createChannel(context)
        val state = DataStoreRepository.getCourseReminderState().first()
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val deliveredKeys = DataStoreRepository.getDeliveredCourseReminders()
        val courses = AppDatabase.getInstance().courseEntityDao()
            .getCurrentSemesterCourseListBySemester(false)
        val allEvents = if (state.enabled && canPostNotifications(context) && canSchedulePrecisely(context)) {
            CourseReminderPlanner.plan(
                courses, state.verifiedWeek, state.verifiedAtMillis, now, state.leadMinutes, zone,
                futureOnly = false,
            ).filterNot { it.key in deliveredKeys }
        } else {
            emptyList()
        }
        val events = allEvents.filter { it.notifyAtMillis > now }
        val overdue = allEvents.filter { CourseReminderPlanner.isDueBeforeClass(it, now, zone) }
            .map { it.notifyAtMillis }.distinct()

        val alarmTimes = events.map { it.notifyAtMillis }.distinct()
        val desiredKeys = alarmTimes.mapTo(mutableSetOf(), ::alarmKey)
        val oldKeys = DataStoreRepository.getScheduledCourseReminders()
        oldKeys.filterNot { it in desiredKeys }.forEach { key ->
            pendingIntent(context, key, 0L, PendingIntent.FLAG_NO_CREATE)?.let { pending ->
                context.getSystemService(AlarmManager::class.java).cancel(pending)
                pending.cancel()
            }
        }

        val manager = context.getSystemService(AlarmManager::class.java)
        val scheduledKeys = mutableSetOf<String>()
        for (notifyAt in alarmTimes) {
            val key = alarmKey(notifyAt)
            val pending = pendingIntent(context, key, notifyAt, PendingIntent.FLAG_UPDATE_CURRENT)
                ?: continue
            try {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyAt, pending)
                scheduledKeys += key
            } catch (_: SecurityException) {
                pending.cancel()
                scheduledKeys.clear()
                break
            }
        }
        DataStoreRepository.setScheduledCourseReminders(scheduledKeys)
        val monday = CourseReminderPlanner.weekStart(Instant.ofEpochMilli(now).atZone(zone).toLocalDate())
        DataStoreRepository.retainDeliveredCourseReminders(monday.toString())
        overdue
    }

    suspend fun deliver(context: Context, key: String, notifyAtMillis: Long) = lock.withLock {
        createChannel(context)
        val state = DataStoreRepository.getCourseReminderState().first()
        if (!state.enabled || !canPostNotifications(context) || key != alarmKey(notifyAtMillis)) return@withLock

        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val deliveredKeys = DataStoreRepository.getDeliveredCourseReminders()
        val courses = AppDatabase.getInstance().courseEntityDao()
            .getCurrentSemesterCourseListBySemester(false)
        val events = CourseReminderPlanner.plan(
            courses, state.verifiedWeek, state.verifiedAtMillis, now, state.leadMinutes, zone,
            futureOnly = false,
        ).filter { it.notifyAtMillis == notifyAtMillis }
        for (event in events) {
            if (!CourseReminderPlanner.isDueBeforeClass(event, now, zone) || event.key in deliveredKeys) continue
            postNotification(context, event, event.key)
            DataStoreRepository.markCourseReminderDelivered(event.key)
        }
    }

    suspend fun suggestTestTime(context: Context): Pair<Int, LocalTime>? = lock.withLock {
        if (!isDebuggable(context)) return@withLock null
        val state = DataStoreRepository.getCourseReminderState().first()
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        if (!isVerifiedThisWeek(state, now, zone)) return@withLock null
        val courses = AppDatabase.getInstance().courseEntityDao()
            .getCurrentSemesterCourseListBySemester(false)
        CourseReminderPlanner.plan(
            courses, state.verifiedWeek, state.verifiedAtMillis, now, state.leadMinutes, zone,
            futureOnly = false,
        ).firstOrNull()?.let { event ->
            event.date.dayOfWeek.value to Instant.ofEpochMilli(event.notifyAtMillis).atZone(zone).toLocalTime()
        }
    }

    suspend fun sendTestNotification(
        context: Context,
        weekday: Int,
        simulatedTime: LocalTime,
    ): TestOutcome = lock.withLock {
        if (!isDebuggable(context)) return@withLock TestOutcome(TestResult.NOT_DEBUG_BUILD)
        createChannel(context)
        val state = DataStoreRepository.getCourseReminderState().first()
        if (!state.enabled) return@withLock TestOutcome(TestResult.REMINDER_DISABLED)
        if (!canPostNotifications(context)) return@withLock TestOutcome(TestResult.NOTIFICATION_PERMISSION_REQUIRED)
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        if (!isVerifiedThisWeek(state, now, zone)) return@withLock TestOutcome(TestResult.WEEK_NOT_VERIFIED)
        if (weekday !in 1..7) return@withLock TestOutcome(TestResult.NO_MATCHING_COURSE)

        val monday = CourseReminderPlanner.weekStart(Instant.ofEpochMilli(now).atZone(zone).toLocalDate())
        val simulatedAt = monday.plusDays((weekday - 1).toLong())
            .atTime(simulatedTime).atZone(zone).toInstant().toEpochMilli()
        val weekStartMillis = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val courses = AppDatabase.getInstance().courseEntityDao()
            .getCurrentSemesterCourseListBySemester(false)
        val matchingEvents = CourseReminderPlanner.plan(
            courses, state.verifiedWeek, weekStartMillis, simulatedAt, state.leadMinutes, zone,
            futureOnly = false,
        ).filter { CourseReminderPlanner.isDueBeforeClass(it, simulatedAt, zone) }
        matchingEvents.forEach { event -> postNotification(context, event, "test|${event.key}") }
        if (matchingEvents.isEmpty()) TestOutcome(TestResult.NO_MATCHING_COURSE)
        else TestOutcome(TestResult.SENT, matchingEvents.size)
    }

    private fun isVerifiedThisWeek(
        state: DataStoreRepository.CourseReminderState,
        nowMillis: Long,
        zone: ZoneId,
    ): Boolean {
        if (state.verifiedWeek <= 0 || state.verifiedAtMillis <= 0 || state.verifiedAtMillis > nowMillis) {
            return false
        }
        val nowDate = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val verifiedDate = Instant.ofEpochMilli(state.verifiedAtMillis).atZone(zone).toLocalDate()
        return CourseReminderPlanner.weekStart(nowDate) == CourseReminderPlanner.weekStart(verifiedDate)
    }

    private fun isDebuggable(context: Context): Boolean =
        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    private fun postNotification(context: Context, event: CourseReminderOccurrence, tag: String) {
        val openSchedule = Intent(context, MainActivity::class.java).apply {
            putExtra(OPEN_SCHEDULE_EXTRA, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context, 0, openSchedule, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val details = compactCoursePlaceText(event.course.coursePlace)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_course_reminder)
            .setContentTitle(event.course.courseName)
            .setContentText(details)
            .setShowWhen(false)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(tag, 0, notification)
    }

    private fun pendingIntent(context: Context, key: String, notifyAtMillis: Long, flags: Int): PendingIntent? {
        val intent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = ACTION_DELIVER
            data = Uri.Builder().scheme("bjtuss").authority("course-reminder")
                .appendPath(key).build()
            putExtra(EXTRA_EVENT_KEY, key)
            putExtra(EXTRA_NOTIFY_AT, notifyAtMillis)
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags or PendingIntent.FLAG_IMMUTABLE)
    }

    fun eventKey(intent: Intent): String? = intent.getStringExtra(EXTRA_EVENT_KEY)
    fun notifyAt(intent: Intent): Long = intent.getLongExtra(EXTRA_NOTIFY_AT, 0L)

    private fun alarmKey(notifyAtMillis: Long): String = "alarm|$notifyAtMillis"
}
