package com.example.campushop.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val department: String = "",
    val year: String = "",
    val createdAt: Long = System.currentTimeMillis()
)