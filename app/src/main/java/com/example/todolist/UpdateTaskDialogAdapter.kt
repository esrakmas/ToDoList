package com.example.todolist

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.todolist.databinding.DialogUpdateTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateTaskDialogAdapter(
    private val context: Context,
    private val task: Task,
    private val onUpdate: (Task) -> Unit
) {
    private val binding = DialogUpdateTaskBinding.inflate(LayoutInflater.from(context))
    private val firebaseHelper = FirebaseHelper()
    private val calendar = Calendar.getInstance()

    val title = binding.updateTaskTitle
    val description = binding.updateTaskDescription
    val spinnerGroup = binding.updateSpinnerTaskGroup
    val customGroup = binding.updateCustomGroup
    val date = binding.updateTaskDate
    val reminder = binding.updateSetReminder

    init {
        setupDialog()
    }

    fun showUpdateTaskDialog() {
        AlertDialog.Builder(context)
            .setTitle("Görevi Güncelle")
            .setView(binding.root)
            .setPositiveButton("Güncelle") { _, _ -> updateTask() }
            .setNegativeButton("İptal", null)
            .create()
            .show()
    }

    private fun setupDialog() {
        // Mevcut verileri doldur
        title.setText(task.title)
        description.setText(task.description)
        date.text = task.dueDate
        reminder.text = task.reminder

        setupDateAndTime()
        setupGroupSelection()
    }

    private fun setupGroupSelection() {
        // Grup verilerini doldur
        val adapter = ArrayAdapter.createFromResource(
            context,
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
            customGroup.visibility = View.VISIBLE
            customGroup.setText(task.group) // Mevcut grup adını EditText'e yaz
            customGroup.isEnabled = true
            customGroup.isFocusable = true
            customGroup.isFocusableInTouchMode = true
            customGroup.requestFocus()
        } else {
            customGroup.visibility = View.GONE
            customGroup.isEnabled = false
        }

        spinnerGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (spinnerGroup.selectedItem.toString() == "Diğer") {
                    customGroup.visibility = View.VISIBLE
                    customGroup.isEnabled = true
                    customGroup.isFocusable = true
                    customGroup.isFocusableInTouchMode = true
                    customGroup.requestFocus()

                    customGroup.setText(task.group)
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

    private fun updateTask() {
        val title = title.text.toString().trim()
        val description = description.text.toString().trim()
        val group = if (spinnerGroup.selectedItem.toString() == "Diğer") {
            customGroup.text.toString().trim()
        } else {
            spinnerGroup.selectedItem.toString().trim()
        }
        val dueDate = date.text.toString().trim()
        val reminder = reminder.text.toString().trim()

        if (title.isNotEmpty() && group.isNotEmpty()) {
            val updatedTask = task.copy(
                title = title,
                description = description,
                group = group,
                dueDate = dueDate,
                reminder = reminder
            )
            firebaseHelper.updateTask(task.id, updatedTask) { success ->
                if (success) {
                    Toast.makeText(context, "Güncelleme başarılı", Toast.LENGTH_SHORT).show()
                    onUpdate(updatedTask) // Callback'i çağır
                    val intent = Intent("TASK_UPDATED")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                } else {
                    Toast.makeText(context, "Güncelleme başarısız", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Başlık ve grup adı girilmelidir.", Toast.LENGTH_SHORT).show()
        }
    }


}
