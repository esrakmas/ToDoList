package com.example.todolist

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.DialogUpdateTaskBinding
import com.example.todolist.databinding.ItemTaskBinding
import java.util.Calendar

//veri çekme
class TasksItemAdapter(private val tasks: List<Task>) :
    RecyclerView.Adapter<TasksItemAdapter.TaskViewHolder>() {

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

            binding.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->

            }

            binding.btnFavorite.setOnCheckedChangeListener { _, isChecked ->

            }

            binding.btnDelete.setOnClickListener {
                showDeleteConfirmationDialog(task.id)
            }

            binding.btnEdit.setOnClickListener {
                // UpdateTaskDialogAdapter kullanarak güncelleme işlemini başlat
                UpdateTaskDialogAdapter(itemView.context, task) { updatedTask ->
                    // Güncellenen görev ile ilgili işlemler yapılabilir
                    // Örneğin, adapter'ı güncelleyebilirsiniz
                    notifyDataSetChanged()
                }.show()
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
