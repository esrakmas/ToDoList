package com.example.todolist

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseHelper {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("tasks")

    fun saveTask(task: Task, onComplete: (Boolean) -> Unit) {
        val taskId = database.push().key
        if (taskId != null) {
            val taskToSave = Task(
                id = taskId,
                title = task.title,
                description = task.description.ifEmpty { "" },
                group = task.group,
                dueDate = task.dueDate.ifEmpty { "" },
                reminder = task.reminder.ifEmpty { "" },
                isCompleted = task.isCompleted,
                isFavorite = task.isFavorite
            )
            database.child(taskId).setValue(taskToSave)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        } else {
            onComplete(false)
        }
    }

    fun updateTask(taskId: String, updatedTask: Task, onComplete: (Boolean) -> Unit) {
        // Güncellenmiş görev verilerini Firebase'e yaz
        database.child(taskId).setValue(updatedTask)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    // Yeni fonksiyon: Task'ı silmek için
    fun deleteTask(taskId: String, onComplete: (Boolean) -> Unit) {
        database.child(taskId).removeValue()
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }



}
