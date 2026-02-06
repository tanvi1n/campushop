package com.example.campushop.data.model

data class Listing(
    val id: String = "",
    val listingId: String = "",
    val userId: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val status: String = "active",
    val condition: String = "Used",
    val priceType: String = "Negotiable",
    val buyerId: String = "",
    val buyerName: String = "",
    val soldAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
