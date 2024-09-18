package com.example.todolist

import android.app.AlertDialog
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
                showUpdateDialog(task)
            }
        }

        private fun showUpdateDialog(task: Task) {
            // ViewBinding ile LayoutInflater kullanarak diyalog görünümünü oluştur
            val dialogBinding = DialogUpdateTaskBinding.inflate(LayoutInflater.from(itemView.context))
            val dialogView = dialogBinding.root

            // EditText ve diğer bileşenlere erişim
            val etTitle = dialogBinding.etTaskTitle
            val etDescription = dialogBinding.etTaskDescription
            val spinnerGroup = dialogBinding.spinnerTaskGroup
            val etCustomGroup = dialogBinding.etCustomGroup
            val txtDueDate = dialogBinding.txtTaskDueDate
            val txtSetReminder = dialogBinding.txtSetReminder

            // Mevcut verileri doldur
            etTitle.setText(task.title)
            etDescription.setText(task.description)
            txtDueDate.text = task.dueDate
            txtSetReminder.text = task.reminder

            // Grup verilerini doldur
            val adapter = ArrayAdapter.createFromResource(
                itemView.context,
                R.array.task_groups,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGroup.adapter = adapter

            // Spinner'daki grup adını seç
            val groupPosition = adapter.getPosition(task.group)
            spinnerGroup.setSelection(groupPosition)

            // Eğer grup "Diğer" ise, EditText görünür ve mevcut grup adı yazılır
            if (task.group == "Diğer") {
                etCustomGroup.visibility = View.VISIBLE
                etCustomGroup.setText(task.group) // Mevcut grup adını EditText'e yaz
                etCustomGroup.isEnabled = true
                etCustomGroup.isFocusable = true
                etCustomGroup.isFocusableInTouchMode = true
                etCustomGroup.requestFocus()
            } else {
                etCustomGroup.visibility = View.GONE
                etCustomGroup.isEnabled = false
            }

            // Spinner'daki seçim değiştiğinde EditText görünürlüğünü ve içeriğini ayarla
            spinnerGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (spinnerGroup.selectedItem.toString() == "Diğer") {
                        etCustomGroup.visibility = View.VISIBLE
                        etCustomGroup.isEnabled = true
                        etCustomGroup.isFocusable = true
                        etCustomGroup.isFocusableInTouchMode = true
                        etCustomGroup.requestFocus()
                        // "Diğer" seçildiğinde mevcut grup adını EditText'e yaz
                        etCustomGroup.setText(task.group)
                    } else {
                        etCustomGroup.visibility = View.GONE
                        etCustomGroup.isEnabled = false
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            // Diyalog oluştur
            AlertDialog.Builder(itemView.context)
                .setTitle("Görevi Güncelle")
                .setView(dialogView)
                .setPositiveButton("Güncelle") { _, _ ->
                    val title = etTitle.text.toString().trim()
                    val description = etDescription.text.toString().trim()
                    val group = if (spinnerGroup.selectedItem.toString() == "Diğer") {
                        etCustomGroup.text.toString().trim()
                    } else {
                        spinnerGroup.selectedItem.toString().trim()
                    }
                    val dueDate = txtDueDate.text.toString().trim()
                    val reminder = txtSetReminder.text.toString().trim()

                    // Başlık ve grup adı boşsa uyarı mesajı göster
                    if (title.isNotEmpty() && group.isNotEmpty()) {
                        val updatedTask = task.copy(
                            title = title,
                            description = description,
                            group = group,
                            dueDate = dueDate,
                            reminder = reminder
                        )
                        updateTask(task.id, updatedTask)
                    } else {
                        Toast.makeText(itemView.context, "Başlık ve grup adı girilmelidir.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .create()
                .show()
        }


        private fun updateTask(taskId: String, updatedTask: Task) {
            firebaseHelper.updateTask(taskId, updatedTask) { success ->
                if (success) {
                    Toast.makeText(binding.root.context, "Güncelleme başarılı", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(binding.root.context, "Güncelleme başarısız", Toast.LENGTH_SHORT).show()
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
