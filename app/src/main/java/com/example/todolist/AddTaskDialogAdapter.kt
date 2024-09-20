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

    init {
        setupDialog()  // Dialog'un ayarlarını kur
    }

    // Görev ekleme diyalogunu gösterir
    fun showAddTaskDialog() {
        AlertDialog.Builder(context)
            .setTitle("Yeni Görev")
            .setView(binding.root)
            .setPositiveButton("Kaydet") { _, _ -> saveTask() }
            .setNegativeButton("İptal", null)
            .create()
            .show()
    }

    // Dialog başlangıç ayarları (Grup seçimi, tarih ve saat seçici)
    private fun setupDialog() {
        setupGroupSelection()  // Grup seçimini kur
        setupDateAndTime()  // Tarih ve saat seçimini kur
    }

    // Görev grubu seçimi (Diğer seçilirse özel grup adı girilebilir)
    private fun setupGroupSelection() {
        binding.addSpinnerTaskGroup.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedGroup = binding.addSpinnerTaskGroup.selectedItem.toString()
                    if (selectedGroup == "Diğer") {
                        // Kullanıcıdan özel bir grup adı girmesini iste
                        binding.addCustomGroup.visibility = View.VISIBLE
                        binding.addCustomGroup.requestFocus()
                        binding.addCustomGroup.setText(task.group)
                    } else {
                        binding.addCustomGroup.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    // Tarih ve saat seçici kurar
    private fun setupDateAndTime() {
        binding.addTaskDate.setOnClickListener { showDatePickerDialog() }
        binding.addSetReminder.setOnClickListener { showTimePickerDialog() }
    }

    // Tarih seçici diyalogunu gösterir
    private fun showDatePickerDialog() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                updateDueDate()  // Tarih alanını güncelle
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Saat seçici diyalogunu gösterir
    private fun showTimePickerDialog() {
        TimePickerDialog(context, { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateReminderTime()  // Hatırlatıcı saatini güncelle
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    // Seçilen tarihi gösterir
    private fun updateDueDate() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.addTaskDate.text = format.format(calendar.time)
    }

    // Seçilen hatırlatma saatini gösterir
    private fun updateReminderTime() {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.addSetReminder.text = format.format(calendar.time)
    }

    // Görevi kaydetmek için gerekli validasyonları yapar ve Firebase'e kaydeder
    private fun saveTask() {
        val title = binding.addTaskTitle.text.toString().trim()
        val description = binding.addTaskDescription.text.toString().trim()
        val group = if (binding.addSpinnerTaskGroup.selectedItem.toString() == "Diğer") {
            binding.addCustomGroup.text.toString().trim()
        } else {
            binding.addSpinnerTaskGroup.selectedItem.toString()
        }
        val dueDate = calendar.timeInMillis  // Kullanıcının seçtiği tarih
        val reminder = calendar.timeInMillis  // Kullanıcının seçtiği hatırlatma saati

        // Görev başlığı ve grup boş değilse kaydet
        if (title.isNotEmpty() && group.isNotEmpty()) {
            val newTask = Task(title, description, group, dueDate= dueDate, reminder, false, false,false)

            saveTaskToFirebase(newTask)
        } else {
            showToast("Görev başlığı giriniz.")
        }
    }

    // Firebase'e görevi kaydeder
    private fun saveTaskToFirebase(task: Task) {
        firebaseHelper.saveTask(task) { success ->
            if (success) {
                showToast("Görev başarıyla kaydedildi.")
                onUpdate(task)  // Görev kaydedildikten sonra tab'ları güncelle
            } else {
                showToast("Görev kaydedilemedi.")
            }
        }
    }

    // Kullanıcıya kısa mesaj gösterir
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
