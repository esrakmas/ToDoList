package com.example.todolist

import AddTaskDialogAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityTasksBinding
import com.example.todolist.databinding.DialogAddTaskBinding

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val firebaseHelper = FirebaseHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FloatingActionButton Click Listener
        binding.fabAddTask.setOnClickListener {
            showTaskDialog()
        }
    }

    private fun showTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogBinding.root

        // AddTaskDialogAdapter oluşturup setup() fonksiyonunu çağırıyoruz
        val adapter = AddTaskDialogAdapter(dialogBinding)
        adapter.setup()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Yeni Görev")
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                val title = dialogBinding.etTaskTitle.text.toString().trim()
                val description = dialogBinding.etTaskDescription.text.toString().trim()
                val group = dialogBinding.spinnerTaskGroup.selectedItem.toString()
                val dueDate = dialogBinding.txtTaskDueDate.text.toString().trim()
                val reminder = dialogBinding.txtSetReminder.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty() && group.isNotEmpty() && dueDate.isNotEmpty() && reminder.isNotEmpty()) {
                    val task = Task(title, description, group, dueDate, reminder)
                    saveTaskToFirebase(task)
                } else {
                    // Show error message if any field is empty
                    showToast("Lütfen tüm alanları doldurun.")
                }
            }
            .setNegativeButton("İptal", null)
            .create()

        dialog.show()
    }

    private fun saveTaskToFirebase(task: Task) {
        firebaseHelper.saveTask(task) { success ->
            if (success) {
                showToast("Görev başarıyla kaydedildi.")
            } else {
                showToast("Görev kaydedilemedi.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
