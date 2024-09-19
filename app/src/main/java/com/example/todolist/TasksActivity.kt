package com.example.todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.todolist.databinding.ActivityTasksBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val firebaseHelper = FirebaseHelper()
    private val tabTitles = mutableSetOf<String>()
    private val groupManager by lazy { GroupManager(this, firebaseHelper) { loadTasks() } }

    // BroadcastReceiver
    private val taskUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadTasks() // Görevler güncellendiğinde çağrılır
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // UI ve dinleyicileri kur
        setupListeners()
        loadTasks()

        // BroadcastReceiver'ı kaydet
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(taskUpdateReceiver, IntentFilter("TASK_UPDATED"))
    }

    // Dinleyicileri kurar (UI için)
    private fun setupListeners() {
        // Yeni görev ekleme butonu
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // Tab seçimi dinleyicisi
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                val selectedGroup = tab.text.toString()
                groupManager.showGroupOptionsDialog(selectedGroup)
            }
        })
    }

    // Görev ekleme diyalogunu gösterir
    private fun showAddTaskDialog() {
        val task = Task()
        val addTaskDialogAdapter = AddTaskDialogAdapter(this, task) {
            loadTasks()  // Görev eklendikten sonra listeyi yeniden yükle
        }
        addTaskDialogAdapter.showAddTaskDialog()  // Görev ekleme dialogunu göster
    }

    // Firebase'den görevleri yükler ve tabları günceller
    private fun loadTasks() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tabTitles.clear()  // Mevcut grupları temizle
                dataSnapshot.children.forEach { snapshot ->
                    val task = snapshot.getValue(Task::class.java)
                    task?.group?.let { group ->
                        tabTitles.add(group)
                    }
                }
                updateTabLayout()  // Yeni verilerle tabları güncelle
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("Veriler yüklenemedi.")  // Hata durumunda mesaj göster
            }
        })
    }

    // TabLayout'u günceller
    private fun updateTabLayout() {
        val adapter = TasksActivityTabManager(this, tabTitles.toList())
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles.toList()[position]
        }.attach()

        // Adaptör verilerini güncelle
        adapter.notifyDataSetChanged()
    }

    // Toast mesajı gösterir
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // BroadcastReceiver'ı kaldır
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(taskUpdateReceiver)
    }
}
