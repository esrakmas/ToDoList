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
import java.util.Calendar

class UpdateTaskDialogAdapter(
    private val context: Context,
    private val task: Task,
    private val onUpdate: (Task) -> Unit
) {

    private val binding = DialogUpdateTaskBinding.inflate(LayoutInflater.from(context))
    private val firebaseHelper = FirebaseHelper()

    init {
        setupDialog()
    }

    fun show() {
        AlertDialog.Builder(context)
            .setTitle("Görevi Güncelle")
            .setView(binding.root)
            .setPositiveButton("Güncelle") { _, _ -> updateTask() }
            .setNegativeButton("İptal", null)
            .create()
            .show()
    }

    private fun setupDialog() {
        val etTitle = binding.etTaskTitle
        val etDescription = binding.etTaskDescription
        val spinnerGroup = binding.spinnerTaskGroup
        val etCustomGroup = binding.etCustomGroup
        val txtDueDate = binding.txtTaskDueDate
        val txtSetReminder = binding.txtSetReminder

        // Mevcut verileri doldur
        etTitle.setText(task.title)
        etDescription.setText(task.description)
        txtDueDate.text = task.dueDate
        txtSetReminder.text = task.reminder

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

        // Due Date seçimi için tıklama olayını ekle
        txtDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "${dayOfMonth}/${month + 1}/${year}"
                    txtDueDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Reminder seçimi için tıklama olayını ekle
        txtSetReminder.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    txtSetReminder.text = selectedTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }
    }

    private fun updateTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()
        val group = if (binding.spinnerTaskGroup.selectedItem.toString() == "Diğer") {
            binding.etCustomGroup.text.toString().trim()
        } else {
            binding.spinnerTaskGroup.selectedItem.toString().trim()
        }
        val dueDate = binding.txtTaskDueDate.text.toString().trim()
        val reminder = binding.txtSetReminder.text.toString().trim()

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
