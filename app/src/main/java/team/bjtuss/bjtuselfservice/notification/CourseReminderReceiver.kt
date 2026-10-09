package team.bjtuss.bjtuselfservice.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val key = CourseReminderScheduler.eventKey(intent) ?: return
        val notifyAt = CourseReminderScheduler.notifyAt(intent)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                CourseReminderScheduler.deliver(context.applicationContext, key, notifyAt)
            } catch (error: Exception) {
                Log.e("CourseReminder", "Failed to deliver course reminder", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
