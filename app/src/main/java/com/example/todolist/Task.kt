package com.example.todolist

data class Task(
    val title: String = "",
    val description: String = "",
    val group: String = "",
    val dueDate: Long = 0L,
    val reminder: Long = 0L,
    val completed: Boolean = false,
    val favorite: Boolean = false,
    val notification: Boolean = false,
    val id: String = "",
    val order: Int = 0
)