package com.example.todolist

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseHelper {

    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("tasks")

    // Yeni bir görev ekler ve tamamlandığında geri bildirim sağlar
    fun saveTask(task: Task, onComplete: (Boolean) -> Unit) {
        val taskId = database.push().key
        if (taskId != null) {
            val taskToSave = Task(
                title = task.title,
                description = task.description.ifEmpty { "" },
                group = task.group,
                dueDate = task.dueDate.ifEmpty { "" },
                reminder = task.reminder.ifEmpty { "" },
                completed = task.completed,
                favorite = task.favorite,
                id = taskId
            )
            database.child(taskId).setValue(taskToSave)
                .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
        } else {
            onComplete(false)
        }
    }

    // Var olan bir görevi günceller
    fun updateTask(taskId: String, updatedTask: Task, callback: (Boolean) -> Unit) {
        database.child(taskId).setValue(updatedTask)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // Bir görevi siler
    fun deleteTask(taskId: String, onComplete: (Boolean) -> Unit) {
        database.child(taskId).removeValue()
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }

    // Belirli bir gruptaki tüm görevleri siler
    fun deleteGroup(group: String, onComplete: (Boolean) -> Unit) {
        val tasksRef = database.orderByChild("group").equalTo(group)
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
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

    // Belirli bir gruptaki tüm görevlerin grup adını günceller
    fun updateGroupName(oldGroup: String, newGroup: String, onComplete: (Boolean) -> Unit) {
        val tasksRef = database.orderByChild("group").equalTo(oldGroup)
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
