package com.example.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.FragmentTaskBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaskFragment : Fragment() {
    private lateinit var binding: FragmentTaskBinding
    private lateinit var tasksAdapter: TasksAdapter
    private val tasksList = mutableListOf<Task>()
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("tasks")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(context)
        tasksAdapter = TasksAdapter(tasksList)
        binding.recyclerViewTasks.adapter = tasksAdapter

        val group = arguments?.getString(ARG_GROUP) ?: ""
        loadTasks(group)
    }

    private fun loadTasks(group: String) {
        database.orderByChild("group").equalTo(group)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tasksList.clear()
                    snapshot.children.forEach { dataSnapshot ->
                        val task = dataSnapshot.getValue(Task::class.java)
                        if (task != null) {
                            tasksList.add(task)
                        }
                    }
                    tasksAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            })
    }

    companion object {
        private const val ARG_GROUP = "arg_group"

        fun newInstance(group: String): TaskFragment {
            val fragment = TaskFragment()
            val args = Bundle()
            args.putString(ARG_GROUP, group)
            fragment.arguments = args
            return fragment
        }
    }
}
