package team.bjtuss.bjtuselfservice.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                CourseReminderScheduler.reconcile(context.applicationContext)
            } catch (error: Exception) {
                Log.e("CourseReminder", "Failed to restore course reminders after ${intent.action}", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
