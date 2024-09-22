package com.example.todolist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.todolist.databinding.ActivityTasksBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.Manifest

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val firebaseHelper = FirebaseHelper()
    private val tabTitles = mutableSetOf<String>()
    private val editTabDialogHelper by lazy { EditTabDialogHelper(this, firebaseHelper) { loadTasks() } }
    private val TAG = "TasksActivity" // Log etiketi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // alarm İzin kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM),
                    1
                )
            }
        }

        // Bildirim izni kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    2
                )
            }
        }
        createNotificationChannel() // Bildirim kanalını oluştur
        setupListeners()
        loadTasks()

        // BroadcastReceiver'ı kaydet
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(taskUpdateReceiver, IntentFilter("TASK_UPDATED"))
    }

    // BroadcastReceiver
    private val taskUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, " görevler yeniden yükleniyor.")
            loadTasks()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> { // SCHEDULE_EXACT_ALARM izni
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // İzin verildi
                } else {
                    // İzin reddedildi
                    Log.d(TAG, " alarm ayarlamak için izin verilmedi..")
                }
            }

            2 -> { // POST_NOTIFICATIONS izni
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // İzin verildi
                } else {
                    // İzin reddedildi
                    Log.d(TAG, " Bildirim göndermek için izin verilmedi")
                }
            }
        }
    }

    private fun setupListeners() {

        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.sortBtn.setOnClickListener {
            val sortDialogHelper = SortDialogHelper(this) { sortOption ->
                handleSortOption(sortOption)
            }
            sortDialogHelper.showSortDialog()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                val selectedGroup = tab.text.toString()
                Log.d(TAG, "Tab reselected: $selectedGroup")
                editTabDialogHelper.showGroupOptionsDialog(selectedGroup)
            }
        })
    }

    private fun handleSortOption(sortOption: SortDialogHelper.SortOption) {
        when (sortOption) {
            SortDialogHelper.SortOption.OLD_TO_NEW -> {
                sortTasksByOldToNew()
            }

            SortDialogHelper.SortOption.NEW_TO_OLD -> {
                sortTasksByNewToOld()
            }

            SortDialogHelper.SortOption.DRAG_AND_DROP -> {
                enableDragAndDrop()
            }
        }
    }

    private fun sortTasksByOldToNew() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks")
        database.orderByChild("dueDate")
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                    tasks.forEachIndexed { index, task ->
                        val taskUpdates = mapOf("order" to index) // order değerini güncelle
                        database.child(task.id).updateChildren(taskUpdates)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Database error: ${databaseError.message}")
                }
            })
    }

    private fun sortTasksByNewToOld() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks")
        database.orderByChild("dueDate")
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                    tasks.forEachIndexed { index, task ->
                        val taskUpdates = mapOf("order" to index) // order değerini güncelle
                        database.child(task.id).updateChildren(taskUpdates)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Database error: ${databaseError.message}")
                }
            })
    }

    private fun enableDragAndDrop() {
        Toast.makeText(
            this,
            "Görevleri sürükleyerek manuel sıralama yapabilirsiniz.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "task_reminder_channel",
            "Hatırlatıcılar",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Görev hatırlatıcıları için kanal"
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Görev ekleme diyalogunu gösterir
    private fun showAddTaskDialog() {
        val task = Task()
        val addTaskDialogHelper = AddTaskDialogHelper(this, task) {
            loadTasks()
        }
        addTaskDialogHelper.showAddTaskDialog()
    }

    // verileri yükler
    private fun loadTasks() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks")
        database.orderByChild("order").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tabTitles.clear()  // Mevcut grupları temizle
                dataSnapshot.children.forEach { snapshot ->
                    val task = snapshot.getValue(Task::class.java)
                    task?.group?.let { group ->
                        tabTitles.add(group)
                        Log.d(TAG, "Group eklendi: $group")
                    }

                }
                updateTabLayout()  // Yeni verilerle tabları güncelle
                Log.d(TAG, "Tab layout güncel, total grup: ${tabTitles.size}")
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
    }

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
