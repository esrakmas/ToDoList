package com.example.todolist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// tab layoutu yönetmek için
class TasksActivityTabManager(activity: FragmentActivity, private val tabTitles: List<String>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return tabTitles.size
    }

    override fun createFragment(position: Int): Fragment {
        return TaskFragment.newInstance(tabTitles[position])
    }
}
