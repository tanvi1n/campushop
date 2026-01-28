package com.example.campushop.data.model

data class Listing(
    val listingId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val status: String = "active", // "active" or "sold"
    val createdAt: Long = System.currentTimeMillis()
)