package com.example.todolist

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.todolist.databinding.DialogUpdateTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateTaskDialogHelper(
    private val context: Context,
    private val task: Task,
    private val onUpdate: (Task) -> Unit
) {
    private val binding = DialogUpdateTaskBinding.inflate(LayoutInflater.from(context))
    private val firebaseHelper = FirebaseHelper()
    private val calendar = Calendar.getInstance()

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
        with(binding) {

            updateTaskTitle.setText(task.title)
            updateTaskDescription.setText(task.description)

            val dueDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val reminderFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            updateTaskDate.text = dueDateFormat.format(task.dueDate)
            updateSetReminder.text = reminderFormat.format(task.reminder)

            setupDateAndTime()
            setupGroupSelection()
        }
    }

    private fun setupGroupSelection() {
        val adapter = ArrayAdapter.createFromResource(
            context,
            R.array.task_groups,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.updateSpinnerTaskGroup.adapter = adapter
        val groupPosition = adapter.getPosition(task.group)
        binding.updateSpinnerTaskGroup.setSelection(groupPosition)

        with(binding) {
            if (task.group == "Diğer") {
                updateCustomGroup.apply {
                    visibility = View.VISIBLE
                    setText(task.group)
                    isEnabled = true
                    requestFocus()
                }
            } else {
                updateCustomGroup.apply {
                    visibility = View.GONE
                    isEnabled = false
                }
            }

            updateSpinnerTaskGroup.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedGroup = updateSpinnerTaskGroup.selectedItem.toString()
                        if (selectedGroup == "Diğer") {
                            updateCustomGroup.apply {
                                visibility = View.VISIBLE
                                isEnabled = true
                                requestFocus()
                                setText(task.group)
                            }
                        } else {
                            updateCustomGroup.apply {
                                visibility = View.GONE
                                isEnabled = false
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }
    }

    private fun setupDateAndTime() {
        with(binding) {
            updateTaskDate.setOnClickListener { showDatePickerDialog() }
            updateSetReminder.setOnClickListener { showTimePickerDialog() }
        }
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
        TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                updateReminderTime()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDueDate() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.updateTaskDate.text = format.format(calendar.time)
    }

    private fun updateReminderTime() {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.updateSetReminder.text = format.format(calendar.time)
    }

    private fun updateTask() {
        val title = binding.updateTaskTitle.text.toString().trim()
        val description = binding.updateTaskDescription.text.toString().trim()
        val group = if (binding.updateSpinnerTaskGroup.selectedItem.toString() == "Diğer") {
            binding.updateCustomGroup.text.toString().trim()
        } else {
            binding.updateSpinnerTaskGroup.selectedItem.toString().trim()
        }

        val dueDate = calendar.timeInMillis
        val reminder = calendar.timeInMillis

        if (title.isNotEmpty() && group.isNotEmpty()) {
            val updatedTask = task.copy(
                title = title,
                description = description,
                group = group,
                dueDate = dueDate,
                reminder = reminder
            )

            firebaseHelper.updateTask(updatedTask.id, updatedTask) { success ->
                if (success) {
                    Toast.makeText(context, "Güncelleme başarılı", Toast.LENGTH_SHORT).show()
                    onUpdate(updatedTask)
                } else {
                    Toast.makeText(context, "Güncelleme başarısız", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Başlık ve grup adı girilmelidir.", Toast.LENGTH_SHORT).show()
        }
    }

}
