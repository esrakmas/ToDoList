package com.example.todolist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.todolist.databinding.DialogAddTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar

import java.util.Locale

class AddTaskDialogAdapter(
    private val context: Context,
    private val task: Task,
    private val onUpdate: (Task) -> Unit
) {
    private val binding = DialogAddTaskBinding.inflate(LayoutInflater.from(context))
    private val firebaseHelper = FirebaseHelper()
    private val calendar = Calendar.getInstance()

    val title = binding.addTaskTitle
    val description = binding.addTaskDescription
    val spinnerGroup = binding.addSpinnerTaskGroup
    val customGroup = binding.addCustomGroup
    val date = binding.addTaskDate
    val reminder = binding.addSetReminder

    init {
        setupDialog()
    }

    fun showAddTaskDialog() {
        AlertDialog.Builder(context)
            .setTitle("Yeni Görev")
            .setView(binding.root)
            .setPositiveButton("Kaydet") { _, _ -> saveTask() }
            .setNegativeButton("İptal", null)
            .create()
            .show()
    }

    private fun setupDialog() {
        setupGroupSelection()
        setupDateAndTime()
    }

    private fun setupGroupSelection() {
        spinnerGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedGroup = spinnerGroup.selectedItem.toString()
                if (selectedGroup == "Diğer") {
                    customGroup.visibility = View.VISIBLE
                    customGroup.requestFocus()
                    customGroup.setText(task.group) // Mevcut grup adını EditText'e yaz
                    customGroup.isEnabled = true
                    customGroup.isFocusable = true
                    customGroup.isFocusableInTouchMode = true
                } else {
                    customGroup.visibility = View.GONE
                    customGroup.isEnabled = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDateAndTime() {
        date.setOnClickListener { showDatePickerDialog() }
        reminder.setOnClickListener { showTimePickerDialog() }
    }

    private fun showDatePickerDialog() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                updateDueDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(context, { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateReminderTime()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun updateDueDate() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        date.text = format.format(calendar.time)
    }

    private fun updateReminderTime() {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        reminder.text = format.format(calendar.time)
    }

    private fun saveTask() {
        val title = title.text.toString().trim()
        val description = description.text.toString().trim()
        val group = if (spinnerGroup.selectedItem.toString() == "Diğer") {
            customGroup.text.toString().trim()
        } else {
            spinnerGroup.selectedItem.toString()
        }
        val dueDate = date.text.toString().trim()
        val reminder = reminder.text.toString().trim()

        if (title.isNotEmpty() && group.isNotEmpty()) {
            val task = Task(title, description, group, dueDate, reminder)
            saveTaskToFirebase(task)
        } else {
            showToast("Görev başlığı giriniz.")
        }
    }

    private fun saveTaskToFirebase(task: Task) {
        firebaseHelper.saveTask(task) { success ->
            if (success) {
                showToast("Görev başarıyla kaydedildi.")
                onUpdate(task)  // Refresh tab layout after saving task
            } else {
                showToast("Görev kaydedilemedi.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
