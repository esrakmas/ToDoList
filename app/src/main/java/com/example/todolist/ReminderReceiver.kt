package com.example.todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import com.google.androidbrowserhelper.locationdelegation.PermissionRequestActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("taskTitle") ?: "Görev"
        val description = intent.getStringExtra("taskDescription") ?: "Bir göreviniz var!"

        // İzin kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // İzin yoksa, bir aktiviteyi başlat
                val notificationIntent = Intent(context, PermissionRequestActivity::class.java).apply {
                    putExtra("taskTitle", title)
                    putExtra("taskDescription", description)
                }
                notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(notificationIntent)
                return
            }
        }

        // İzin verildiyse bildirim gönder
        sendNotification(context, title, description)
    }

    private fun sendNotification(context: Context, title: String, description: String) {
        val notificationId = System.currentTimeMillis().toInt()
        val builder = NotificationCompat.Builder(context, "task_reminder_channel")
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }
}
