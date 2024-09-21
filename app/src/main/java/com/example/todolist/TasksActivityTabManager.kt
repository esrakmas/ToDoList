package com.example.todolist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// Tab Layout'u yönetmek için kullanılan adapter
class TasksActivityTabManager(
    activity: FragmentActivity,
    private val tabTitles: List<String>  // Tab başlıklarını tutan liste
) : FragmentStateAdapter(activity) {



    // Tab sayısını döndürür
    override fun getItemCount(): Int {
        return tabTitles.size
    }

    // Pozisyona göre uygun fragment'ı oluşturur
    override fun createFragment(position: Int): Fragment {
        return TaskFragment.newInstance(tabTitles[position])
    }

}
