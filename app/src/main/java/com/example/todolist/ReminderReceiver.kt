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
import android.util.Log
import com.google.androidbrowserhelper.locationdelegation.PermissionRequestActivity
import com.google.firebase.database.FirebaseDatabase

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("taskTitle") ?: "Görev"
        val description = intent.getStringExtra("taskDescription") ?: "Bir göreviniz var!"
        val taskId = intent.getStringExtra("taskId") // Görev ID'sini al

        // İzin kontrolü ve bildirim gönderimi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // İzin yoksa, bildirim göndermeden çık
                return
            }
        }

        // Bildirim gönder
        sendNotification(context, title, description)

        // Firebase'de notification değerini false olarak güncelle


        taskId?.let {
            val database = FirebaseDatabase.getInstance().reference.child("tasks").child(it)
            database.child("notification").setValue(false).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ReminderReceiver", "Notification status updated successfully.")
                } else {
                    Log.e(
                        "ReminderReceiver",
                        "Failed to update notification status: ${task.exception?.message}"
                    )
                }
            }
        } ?: run {
            Log.e("ReminderReceiver", "Task ID is null, cannot update notification status.")

        }

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

