package com.example.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoriteTasksAdapter(private val favoriteTasks: List<Task>, private val onTaskSelected: (Task) -> Unit) : RecyclerView.Adapter<FavoriteTasksAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textViewTaskTitle)
        val descriptionTextView: TextView = view.findViewById(R.id.textViewTaskDescription)

        init {
            view.setOnClickListener {
                onTaskSelected(favoriteTasks[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = favoriteTasks[position]
        holder.titleTextView.text = task.title
        holder.descriptionTextView.text = task.description
    }

    override fun getItemCount() = favoriteTasks.size
}
