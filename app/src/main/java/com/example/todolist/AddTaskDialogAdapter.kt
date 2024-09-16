package com.example.todolist


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import com.example.todolist.databinding.DialogAddTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskDialogAdapter(private val binding: DialogAddTaskBinding) {

    private val calendar = Calendar.getInstance()

    fun setup() {
        // Tarih TextView'e tıklanınca DatePicker aç
        binding.txtTaskDueDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Hatırlatıcı TextView'e tıklanınca TimePicker aç
        binding.txtSetReminder.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            binding.root.context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDueDate()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            binding.root.context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateReminderTime()
            },
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        )
        timePickerDialog.show()
    }

    private fun updateDueDate() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.txtTaskDueDate.text = format.format(calendar.time)
    }

    private fun updateReminderTime() {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.txtSetReminder.text = format.format(calendar.time)
    }
}