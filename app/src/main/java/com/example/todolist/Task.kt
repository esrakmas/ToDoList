package com.example.todolist

data class Task(
    val title: String = "",
    val description: String = "",
    val group: String = "",
    val dueDate: String = "",
    val reminder: String = "",
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,
    val id:String=""
)