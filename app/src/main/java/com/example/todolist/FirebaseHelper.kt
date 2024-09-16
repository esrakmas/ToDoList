package com.example.todolist

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseHelper {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("tasks")

    fun saveTask(task: Task, onComplete: (Boolean) -> Unit) {
        val taskId = database.push().key
        if (taskId != null) {
            val taskToSave = Task(
                title = task.title,
                description = task.description.ifEmpty { "" },
                group = task.group,
                dueDate = task.dueDate.ifEmpty { "" },
                reminder = task.reminder.ifEmpty { "" }
            )
            database.child(taskId).setValue(taskToSave)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        } else {
            onComplete(false)
        }
    }
}
