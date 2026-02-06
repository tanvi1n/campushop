package com.example.campushop.data.model

data class Transaction(
    val id: String = "",
    val listingId: String = "",
    val listingTitle: String = "",
    val listingImageUrl: String = "",
    val price: Double = 0.0,
    val sellerId: String = "",
    val sellerName: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
