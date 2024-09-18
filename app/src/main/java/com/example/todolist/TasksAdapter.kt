package com.example.todolist

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.ItemTaskBinding

//veri çekme
class TasksAdapter(private val tasks: List<Task>) :
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    private val firebaseHelper = FirebaseHelper()

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemTaskBinding.bind(itemView)

        fun bind(task: Task) {
            binding.taskTitle.text = task.title
            binding.taskDescription.text = task.description
            binding.taskDueDate.text = task.dueDate
            binding.taskSetReminder.text = task.reminder

            binding.taskCheckbox.isChecked = task.isCompleted
            binding.btnFavorite.isChecked = task.isFavorite

            // Eşzamanlı güncellemeleri engellemek için bu değişiklikleri uygulayabilirsiniz
            binding.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (binding.btnFavorite.isPressed) return@setOnCheckedChangeListener // Favori butonu basılıysa, CheckBox'ı işlemi atla
                updateTask(task, isCompleted = isChecked)
            }

            binding.btnFavorite.setOnCheckedChangeListener { _, isChecked ->
                if (binding.taskCheckbox.isPressed) return@setOnCheckedChangeListener // CheckBox basılıysa, Favori butonunu işlemi atla
                updateTask(task, isFavorite = isChecked)
            }

            binding.btnDelete.setOnClickListener {
                showDeleteConfirmationDialog(task.id)
            }
        }

        private fun updateTask(
            task: Task,
            isCompleted: Boolean? = null,
            isFavorite: Boolean? = null
        ) {
            val updatedTask = task.copy(
                isCompleted = isCompleted ?: task.isCompleted,
                isFavorite = isFavorite ?: task.isFavorite
            )
            firebaseHelper.updateTask(task.id, updatedTask) { success ->
                if (success) {
                    Toast.makeText(binding.root.context, "Güncelleme başarılı", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(binding.root.context, "Güncelleme başarısız", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        private fun showDeleteConfirmationDialog(taskId: String) {
            AlertDialog.Builder(binding.root.context)
                .setTitle("Silme Onayı")
                .setMessage("Bu görevi silmek istediğinizden emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    deleteTask(taskId)
                }
                .setNegativeButton("Hayır", null)
                .show()
        }

        private fun deleteTask(taskId: String) {
            firebaseHelper.deleteTask(taskId) { success ->
                if (success) {
                    Toast.makeText(binding.root.context, "Görev silindi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(binding.root.context, "Görev silme başarısız", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size
}
