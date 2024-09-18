package com.example.todolist

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

    fun updateTask(taskId: String, updatedTask: Task, callback: (Boolean) -> Unit) {
        database.child(taskId).setValue(updatedTask)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    // Yeni fonksiyon: Task'ı silmek için
    fun deleteTask(taskId: String, onComplete: (Boolean) -> Unit) {
        database.child(taskId).removeValue()
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }


    fun deleteGroup(group: String, onComplete: (Boolean) -> Unit) {
        val tasksRef = FirebaseDatabase.getInstance().reference.child("tasks")
        tasksRef.orderByChild("group").equalTo(group)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (taskSnapshot in snapshot.children) {
                        taskSnapshot.ref.removeValue()  // Gruplara ait görevleri sil
                    }
                    onComplete(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false)
                }
            })
    }



    fun updateGroupName(oldGroup: String, newGroup: String, onComplete: (Boolean) -> Unit) {
        val tasksRef = FirebaseDatabase.getInstance().reference.child("tasks")
        tasksRef.orderByChild("group").equalTo(oldGroup)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (taskSnapshot in snapshot.children) {
                        taskSnapshot.ref.child("group").setValue(newGroup)  // Grup adını güncelle
                    }
                    onComplete(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false)
                }
            })
    }




}
