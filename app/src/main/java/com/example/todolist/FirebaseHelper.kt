package com.example.todolist

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseHelper {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun saveTask(task: Task, onComplete: (Boolean) -> Unit) {
        val taskId = database.child("tasks").push().key
        if (taskId != null) {
            database.child("tasks").child(taskId).setValue(task)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        } else {
            onComplete(false)
        }
    }
}
