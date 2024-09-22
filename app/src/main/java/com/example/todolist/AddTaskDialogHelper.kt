package com.example.todolist


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.DialogAddTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskDialogHelper(
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
        if (task.id.isNotEmpty()) {
            loadTaskDetails() // Mevcut görev bilgilerini yükle
        }

        // Favori görevleri yükle
        loadFavoriteTasks()

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
                        binding.addCustomGroup.isEnabled = true
                        binding.addCustomGroup.setText(task.group)
                    } else {
                        binding.addCustomGroup.visibility = View.GONE
                        binding.addCustomGroup.isEnabled = false
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }


    // Seçilen hatırlatma saatini gösterir
    private fun updateReminderTime() {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())

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
            val newTask = Task(title, description, group, dueDate = dueDate, reminder = reminder)

            saveTaskToFirebase(newTask)
        } else {
            showToast("Görev başlığı giriniz.")
        }
    }

    // Firebase'e görevi kaydeder
    private fun saveTaskToFirebase(task: Task) {
        firebaseHelper.saveTask(task, context) { success ->
            if (success) {
                showToast("Görev başarıyla kaydedildi.")
                onUpdate(task)  // Görev kaydedildikten sonra tab'ları güncelle
            } else {
                showToast("Görev kaydedilemedi.")
            }
        }
    }

    private fun loadFavoriteTasks() {
        val firebaseHelper = FirebaseHelper()
        firebaseHelper.getFavoriteTasks { taskList ->
            Log.d("FavoriteTasks", "Gelen görevler: ${taskList.size}")
            val favoriteTasks = taskList.filter { it.favorite }
            setupRecyclerView(favoriteTasks)
        }
    }

    private fun setupRecyclerView(favoriteTasks: List<Task>) {
        binding.recyclerViewFavoriteTasks.layoutManager = LinearLayoutManager(context)

        val adapter = FavoriteTasksAdapter(favoriteTasks) { task ->
            // Favori görev seçildiğinde AddTaskDialogAdapter'ı aç
            val dialog = AddTaskDialogHelper(context, task) { updatedTask ->
                // Güncelleme sonrası yapılacak işlemler
            }
            dialog.showAddTaskDialog()
        }
        binding.recyclerViewFavoriteTasks.adapter = adapter
        adapter.notifyDataSetChanged()
    }


    // Mevcut görev bilgilerini dialoga yükler
    private fun loadTaskDetails() {
        binding.addTaskTitle.setText(task.title)
        binding.addTaskDescription.setText(task.description)
        binding.addSpinnerTaskGroup.setSelection(getGroupIndex(task.group))
        updateReminderTime()  // Hatırlatma zamanını güncelle
    }

    // Grup adının indexini bul
    private fun getGroupIndex(group: String): Int {
        val groups = context.resources.getStringArray(R.array.task_groups)
        return groups.indexOf(group)
    }

    // Kullanıcıya kısa mesaj gösterir
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
