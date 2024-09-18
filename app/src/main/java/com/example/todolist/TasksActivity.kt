package com.example.todolist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.example.todolist.databinding.ActivityTasksBinding
import com.example.todolist.databinding.DialogAddTaskBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.tabs.TabLayoutMediator

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val firebaseHelper = FirebaseHelper()
    private val tabTitles = mutableSetOf<String>()  // to keep track of unique tab titles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddTask.setOnClickListener {
            showTaskDialog()
        }

        loadTasks()
    }

    private fun showTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogBinding.root

        // AddTaskDialogAdapter oluşturup setup() fonksiyonunu çağırıyoruz
        val adapter = AddTaskDialogAdapter(dialogBinding)
        adapter.setup()


        // Spinner'daki seçime göre EditText'in görünürlüğünü ayarlama
        dialogBinding.spinnerTaskGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (dialogBinding.spinnerTaskGroup.selectedItem.toString() == "Diğer") {
                    dialogBinding.etCustomGroup.visibility = View.VISIBLE
                    dialogBinding.etCustomGroup.isEnabled = true
                    dialogBinding.etCustomGroup.isFocusable = true
                    dialogBinding.etCustomGroup.isFocusableInTouchMode = true
                    dialogBinding.etCustomGroup.requestFocus()

                } else {
                    dialogBinding.etCustomGroup.visibility = View.GONE
                    dialogBinding.etCustomGroup.isEnabled = false
                    dialogBinding.etCustomGroup.isFocusable = false

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        val dialog = AlertDialog.Builder(this)
            .setTitle("Yeni Görev")
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                val title = dialogBinding.etTaskTitle.text.toString().trim()
                val description = dialogBinding.etTaskDescription.text.toString().trim()
                val group = if (dialogBinding.spinnerTaskGroup.selectedItem.toString() == "Diğer") {
                    dialogBinding.etCustomGroup.text.toString().trim()  // "Diğer" ise EditText'ten grup adı al
                } else {
                    dialogBinding.spinnerTaskGroup.selectedItem.toString()  // Diğer durumlarda Spinner'dan grup adı al
                }
                val dueDate = dialogBinding.txtTaskDueDate.text.toString().trim()
                val reminder = dialogBinding.txtSetReminder.text.toString().trim()

                if (title.isNotEmpty() && group.isNotEmpty()) {
                    val task = Task(title, description, group, dueDate, reminder)
                    saveTaskToFirebase(task)
                } else {
                    showToast("Görev başlığı giriniz.")
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
                loadTasks()  // Refresh tab layout after saving task
            } else {
                showToast("Görev kaydedilemedi.")
            }
        }
    }

    private fun loadTasks() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tabTitles.clear()  // Clear previous titles
                dataSnapshot.children.forEach { snapshot ->
                    val task = snapshot.getValue(Task::class.java)
                    task?.group?.let { group ->
                        tabTitles.add(group)  // Add group name to the set
                    }
                }
                updateTabLayout()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("Veriler yüklenemedi.")
            }
        })
    }

    private fun updateTabLayout() {
        val adapter = TasksActivityTabManager(this, tabTitles.toList())
        binding.viewPager.adapter = adapter

        // Use TabLayoutMediator to set up the TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles.toList()[position]
        }.attach()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
