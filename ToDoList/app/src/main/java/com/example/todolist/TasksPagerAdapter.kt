package com.example.todolist


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.todolist.TaskFragment

class TasksPagerAdapter(activity: FragmentActivity, private val tabTitles: List<String>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return tabTitles.size
    }

    override fun createFragment(position: Int): Fragment {
        val group = tabTitles[position]
        return TaskFragment.newInstance(group)
    }
}
