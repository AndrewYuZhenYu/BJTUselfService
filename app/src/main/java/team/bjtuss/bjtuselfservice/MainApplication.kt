package team.bjtuss.bjtuselfservice

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import team.bjtuss.bjtuselfservice.database.AppDatabase
import team.bjtuss.bjtuselfservice.notification.CourseReminderScheduler
import team.bjtuss.bjtuselfservice.repository.DataStoreRepository

@HiltAndroidApp
class MainApplication : Application() {
    private val reminderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(FlowPreview::class)
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        CourseReminderScheduler.createChannel(this)
        reminderScope.launch {
            combine(
                AppDatabase.getInstance().courseEntityDao().getCurrentSemesterCourseBySemester(false),
                DataStoreRepository.getCourseReminderState(),
            ) { _, _ -> Unit }.debounce(300).collect {
                CourseReminderScheduler.reconcile(applicationContext)
            }
        }
    }

    fun refreshCourseReminders() {
        reminderScope.launch { CourseReminderScheduler.reconcile(applicationContext) }
    }

    companion object {
        lateinit var appContext: Context
            private set

    }
}
