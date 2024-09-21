package com.example.todolist

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksItemAdapter(private val tasks: MutableList<Task>) :
    RecyclerView.Adapter<TasksItemAdapter.TaskViewHolder>() {

    private val firebaseHelper = FirebaseHelper()

    // ViewHolder tanımı
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemTaskBinding.bind(itemView)
        private val context: Context = itemView.context
        // Veriyi View'a bağlar
        fun bind(task: Task) {
            bindTaskData(task)
            setTaskListeners(task)
        }

        // Görev verilerini layout'a bağlar
        private fun bindTaskData(task: Task) {
            binding.taskTitle.text = task.title
            binding.taskDescription.text = task.description

            // Long türündeki dueDate ve reminder'ı String formatında göster
            val dueDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val reminderFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            binding.taskDueDate.text = dueDateFormat.format(task.dueDate)
            binding.taskSetReminder.text = reminderFormat.format(task.reminder)

            binding.taskCheckbox.isChecked = task.completed
            binding.btnFavorite.isChecked = task.favorite
            binding.btnReminder.isChecked = task.notification
        }

        // Görev ile ilgili listener'ları kurar
        private fun setTaskListeners(task: Task) {
            binding.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
                updateTaskStatus(task.copy(completed = isChecked)) // Görev durumu güncelleme
            }

            binding.btnFavorite.setOnCheckedChangeListener { _, isChecked ->
                updateTaskStatus(task.copy(favorite = isChecked)) // Görev favori durumu güncelleme
            }

            binding.btnReminder.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showReminderDialog(task) // Hatırlatıcı diyalogunu göster

                } else {
                    updateTaskStatus(task.copy(notification = false)) // Hatırlatıcıyı kaldır
                }
            }

            binding.btnDelete.setOnClickListener {
                showDeleteConfirmationDialog(task.id) // Silme işlemi
            }

            binding.btnEdit.setOnClickListener {
                showEditTaskDialog(task) // Düzenleme işlemi
            }
        }

        // Hatırlatıcı ekleme diyalogunu gösterir
        private fun showReminderDialog(task: Task) {
            val builder = AlertDialog.Builder(binding.root.context)
            builder.setTitle("Hatırlatıcı Ekle")

            val view = LayoutInflater.from(binding.root.context).inflate(R.layout.dialog_set_reminder, null)
            builder.setView(view)

            // Tarih ve saat seçicileri
            val datePicker = view.findViewById<DatePicker>(R.id.datePicker)
            val timePicker = view.findViewById<TimePicker>(R.id.timePicker)

            builder.setPositiveButton("Hatırlatıcı Ekle") { _, _ ->
                val calendar = Calendar.getInstance()
                calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth,
                    timePicker.hour, timePicker.minute, 0)

                val reminderTime = calendar.timeInMillis
                // Görev için hatırlatıcıyı güncelle
                updateTaskStatus(task.copy(reminder = reminderTime, notification = true))
                setReminderNotification(task, reminderTime) // Bildirimi ayarlama
            }

            builder.setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
                binding.btnReminder.isChecked = false // Tıklanmadan kalsın
            }

            val dialog = builder.create()
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.show()
        }


        // Bildirim oluşturma
        private fun setReminderNotification(task: Task, reminderTime: Long) {
            // Bildirim zamanlayıcı ayarla
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("taskTitle", task.title)
                putExtra("taskDescription", task.description)
            }
            val pendingIntent = PendingIntent.getBroadcast(context, task.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            // Bildirim zamanını ayarla
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            }
        }


        // Görev durumunu günceller
        private fun updateTaskStatus(updatedTask: Task) {
            firebaseHelper.updateTask(updatedTask.id, updatedTask) { success ->
                if (success) {
                    val position = tasks.indexOfFirst { it.id == updatedTask.id }
                    if (position != -1) {
                        tasks[position] = updatedTask
                        notifyItemChanged(position)
                    }
                }
            }
        }

        // Görev düzenleme diyalogunu açar
        private fun showEditTaskDialog(task: Task) {
            UpdateTaskDialogAdapter(itemView.context, task) { updatedTask ->
                updateTask(updatedTask)
            }.showUpdateTaskDialog()
        }

        // Görev silme onay diyalogunu gösterir
        private fun showDeleteConfirmationDialog(taskId: String) {
            AlertDialog.Builder(binding.root.context)
                .setTitle("Silme Onayı")
                .setMessage("Bu görevi silmek istediğinizden emin misiniz?")
                .setPositiveButton("Evet") { _, _ -> deleteTask(taskId) }
                .setNegativeButton("Hayır", null)
                .show()
        }

        // Görevi siler
        private fun deleteTask(taskId: String) {
            firebaseHelper.deleteTask(taskId) { success ->
                if (success) {
                    onTaskDeleted(taskId)
                } else {
                    Toast.makeText(binding.root.context, "Görev silme başarısız", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Görev silme işlemi tamamlandığında yapılacaklar
        private fun onTaskDeleted(taskId: String) {
            Toast.makeText(binding.root.context, "Görev silindi", Toast.LENGTH_SHORT).show()
            val position = tasks.indexOfFirst { it.id == taskId }
            if (position != -1) {
                tasks.removeAt(position)
                notifyItemRemoved(position)

                // Diğer aktiviteleri güncellemek için broadcast gönder
                val intent = Intent("TASK_UPDATED")
                LocalBroadcastManager.getInstance(binding.root.context).sendBroadcast(intent)
            }
        }
    }

    // ViewHolder oluşturulması
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    // ViewHolder'a veriyi bağlar
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    // Görevlerin sayısı
    override fun getItemCount(): Int = tasks.size

    // Görevi günceller
    fun updateTask(updatedTask: Task) {
        val position = tasks.indexOfFirst { it.id == updatedTask.id }
        if (position != -1) {
            tasks[position] = updatedTask
            notifyItemChanged(position)
        }
    }
}
