package com.example.todolist

import AddTaskDialogAdapter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityTasksBinding
import com.example.todolist.databinding.DialogAddTaskBinding
import java.text.SimpleDateFormat
import java.util.*


class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding

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

        // Görev kaydetme dialogunu oluşturuyoruz
        val dialog = AlertDialog.Builder(this)
            .setTitle("Yeni Görev")
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                // Görev kaydetme işlemleri burada yapılacak
            }
            .setNegativeButton("İptal", null)
            .create()

        dialog.show()
    }
}