import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.timekeeper.adapters.NotificationAdapter
import com.example.timekeeper.database.Reminder
import com.example.timekeeper.database.ReminderDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    val workerContext = context

    override fun doWork(): Result {
        val color = inputData.getString("color") ?: "#FF0FA2E6"
        val title = inputData.getString("title") ?: "Event Title"
        val description = inputData.getString("description") ?: "Event Description"
        val eventId = inputData.getInt("event_id", 0)

        // Database setup
        val db = ReminderDatabase.getDatabase(workerContext)
        val dao = db.reminderDao()

        GlobalScope.launch {
            val reminderId = dao.upsertReminder(
                Reminder(
                    0,
                    color,
                    title,
                    description,
                    LocalDateTime.now(),
                    eventId
                )
            )

            // Shows a new notification referring to the previously created reminder
            NotificationAdapter.createAndShowNotification(
                workerContext,
                color,
                title,
                description,
                reminderId.toInt()
            )
        }

        return Result.success()
    }
}
