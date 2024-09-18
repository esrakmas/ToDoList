package com.example.todolist

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityTasksBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.tabs.TabLayoutMediator

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val firebaseHelper = FirebaseHelper()
    private val tabTitles = mutableSetOf<String>()
    private val groupManager by lazy { GroupManager(this, firebaseHelper) { loadTasks() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        loadTasks()
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                val selectedGroup = tab.text.toString()
                groupManager.showGroupOptionsDialog(selectedGroup)
            }
        })
    }

    private fun showAddTaskDialog() {
        val task = Task()
        val addTaskDialogAdapter = AddTaskDialogAdapter(this, task) {
            loadTasks()  // Görevleri yeniden yükle
        }
        addTaskDialogAdapter.showAddTaskDialog()  // Görev ekleme dialogunu göster
    }

    private fun loadTasks() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks")
        database.get().addOnSuccessListener { dataSnapshot ->
            tabTitles.clear()
            dataSnapshot.children.forEach { snapshot ->
                val task = snapshot.getValue(Task::class.java)
                task?.group?.let { group ->
                    tabTitles.add(group)
                }
            }
            updateTabLayout()
        }.addOnFailureListener {
            showToast("Veriler yüklenemedi.")
        }
    }

    private fun updateTabLayout() {
        val adapter = TasksActivityTabManager(this, tabTitles.toList())
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles.toList()[position]
        }.attach()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
