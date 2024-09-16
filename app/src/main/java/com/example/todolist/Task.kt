package com.example.todolist

data class Task(
    val title: String = "",
    val description: String = "",
    val group: String = "",
    val dueDate: String = "",
    val reminder: String = ""
) {
    // Parametresiz kurucu
    constructor() : this("", "", "", "", "")
}
