package com.example.lab4

data class TodoItem(
    val id: Long,           // Database ID
    val text: String,       // Task description
    val isUrgent: Boolean   // Priority flag
)