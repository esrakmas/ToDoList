package com.example.todolist

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent


    private val TAG = "TasksActivity" // Log etiketi

    // BroadcastReceiver
    private val taskUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Broadcast received, reloading tasks.")
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
        Log.d(TAG, "BroadcastReceiver registered.")
    }





    // Dinleyicileri kurar (UI için)
    private fun setupListeners() {
        // Yeni görev ekleme butonu
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // Sıralama butonuna tıklama dinleyicisi
        binding.sortBtn.setOnClickListener {
            val sortDialogHelper = SortDialogHelper(this) { sortOption ->
                handleSortOption(sortOption)
            }
            sortDialogHelper.showSortDialog()
        }

        // Tab seçimi dinleyicisi
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                val selectedGroup = tab.text.toString()
                Log.d(TAG, "Tab reselected: $selectedGroup")
                groupManager.showGroupOptionsDialog(selectedGroup)
            }
        })
    }
    private fun handleSortOption(sortOption: SortDialogHelper.SortOption) {
        when (sortOption) {
            SortDialogHelper.SortOption.OLD_TO_NEW -> {
                // Eski görevleri yeniye doğru sıralayın
                sortTasksByOldToNew()
            }
            SortDialogHelper.SortOption.NEW_TO_OLD -> {
                // Yeni görevleri eskiye doğru sıralayın
                sortTasksByNewToOld()
            }
            SortDialogHelper.SortOption.DRAG_AND_DROP -> {
                // Sürükleyerek sıralama işlemini başlat
                enableDragAndDrop()
            }
        }
    }

    private fun sortTasksByOldToNew() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks")
        database.orderByChild("dueDate").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tasks = mutableListOf<Task>()
                dataSnapshot.children.forEach { snapshot ->
                    val task = snapshot.getValue(Task::class.java)
                    if (task != null) {
                        tasks.add(task)
                    }
                }
                // Görevleri dueDate değerine göre eskiye doğru sıralayın
                tasks.sortBy { it.dueDate }
                Log.d(TAG, "Sorted tasks (Old to New): ${tasks.map { it.title }}") // Log ekle

                // Her bir görevin order değerini güncelle
                tasks.forEachIndexed { index, task ->
                    val taskUpdates = mapOf("order" to index) // order değerini güncelle
                    database.child(task.id).updateChildren(taskUpdates)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("Veriler yüklenemedi.")
                Log.e(TAG, "Database error: ${databaseError.message}") // Hata logu ekle
            }
        })
    }

    private fun sortTasksByNewToOld() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks")
        database.orderByChild("dueDate").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tasks = mutableListOf<Task>()
                dataSnapshot.children.forEach { snapshot ->
                    val task = snapshot.getValue(Task::class.java)
                    if (task != null) {
                        tasks.add(task)
                    }
                }
                // Görevleri dueDate değerine göre yeniye doğru sıralayın
                tasks.sortByDescending { it.dueDate }
                Log.d(TAG, "Sorted tasks (New to Old): ${tasks.map { it.title }}") // Log ekle

                // Her bir görevin order değerini güncelle
                tasks.forEachIndexed { index, task ->
                    val taskUpdates = mapOf("order" to index) // order değerini güncelle
                    database.child(task.id).updateChildren(taskUpdates)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("Veriler yüklenemedi.")
                Log.e(TAG, "Database error: ${databaseError.message}") // Hata logu ekle
            }
        })
    }




    private fun enableDragAndDrop() {
        // Burada sürükleyerek sıralama özelliğini etkinleştirin
        // RecyclerView veya başka bir bileşen üzerinde gerekli ayarlamaları yapın
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
        Log.d(TAG, "Loading tasks from Firebase.")
        database.orderByChild("order").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tabTitles.clear()  // Mevcut grupları temizle
                dataSnapshot.children.forEach { snapshot ->
                    val task = snapshot.getValue(Task::class.java)
                    task?.group?.let { group ->
                        tabTitles.add(group)
                        Log.d(TAG, "Group added: $group") }


                }
                updateTabLayout()  // Yeni verilerle tabları güncelle
                Log.d(TAG, "Tab layout updated, total groups: ${tabTitles.size}")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("Veriler yüklenemedi.")  // Hata durumunda mesaj göster
                Log.e(TAG, "Database error: ${databaseError.message}")
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
        Log.d(TAG, "TabLayout updated with new adapter.")
    }

    // Toast mesajı gösterir
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // BroadcastReceiver'ı kaldır
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(taskUpdateReceiver)
        Log.d(TAG, "BroadcastReceiver unregistered.")
    }
}
