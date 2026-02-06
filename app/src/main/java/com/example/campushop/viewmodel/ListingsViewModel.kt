package com.example.campushop.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushop.data.CloudinaryHelper
import com.example.campushop.data.model.Listing
import com.example.campushop.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ListingsViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val authRepository = AuthRepository()

    // All listings
    private val _allListings = MutableStateFlow<List<Listing>>(emptyList())
    val allListings: StateFlow<List<Listing>> = _allListings

    // My listings
    private val _myListings = MutableStateFlow<List<Listing>>(emptyList())
    val myListings: StateFlow<List<Listing>> = _myListings

    // Selected listing for detail view
    private val _selectedListing = MutableStateFlow<Listing?>(null)
    val selectedListing: StateFlow<Listing?> = _selectedListing

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    init {
        Log.d("ListingsViewModel", "ViewModel initialized")
        fetchAllListings()
        fetchMyListings()
    }

    // Fetch all active listings
    fun fetchAllListings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("ListingsViewModel", "Starting to fetch all listings...")

                val snapshot = firestore.collection("listings")
                    .whereEqualTo("status", "active")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                Log.d("ListingsViewModel", "Fetched ${snapshot.documents.size} documents from Firestore")

                val listings = snapshot.documents.mapNotNull { doc ->
                    Log.d("ListingsViewModel", "Document ID: ${doc.id}")
                    Log.d("ListingsViewModel", "Document data: ${doc.data}")
                    doc.toObject(Listing::class.java)?.copy(listingId = doc.id)
                }

                Log.d("ListingsViewModel", "Converted to ${listings.size} listing objects")
                listings.forEach { listing ->
                    Log.d("ListingsViewModel", "Listing: ${listing.title} - ₹${listing.price}")
                }

                _allListings.value = listings
                _isLoading.value = false

                Log.d("ListingsViewModel", "Successfully updated allListings state")

            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error fetching all listings", e)
                _errorMessage.value = "Failed to load listings: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Fetch my listings
    fun fetchMyListings() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId() ?: return@launch

                Log.d("ListingsViewModel", "Fetching listings for user: $userId")

                val snapshot = firestore.collection("listings")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                Log.d("ListingsViewModel", "Fetched ${snapshot.documents.size} of my listings")

                val listings = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Listing::class.java)?.copy(listingId = doc.id)
                }

                _myListings.value = listings
                Log.d("ListingsViewModel", "My listings count: ${listings.size}")

            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error fetching my listings", e)
                _errorMessage.value = "Failed to load your listings: ${e.message}"
            }
        }
    }

    // Create new listing
    fun createListing(
        title: String,
        description: String,
        category: String,
        price: Double,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("ListingsViewModel", "Starting to create listing: $title")

                val userId = authRepository.getCurrentUserId()
                    ?: throw Exception("User not logged in")

                Log.d("ListingsViewModel", "User ID: $userId")
                Log.d("ListingsViewModel", "Uploading image to Cloudinary...")

                // Upload image to Cloudinary
                val imageUrl = CloudinaryHelper.uploadImage(imageUri)

                Log.d("ListingsViewModel", "Image uploaded successfully: $imageUrl")

                // Create listing object
                val listing = Listing(
                    userId = userId,
                    sellerId = userId,
                    sellerName = authRepository.getCurrentUserName() ?: "Anonymous",
                    title = title,
                    description = description,
                    category = category,
                    price = price,
                    imageUrl = imageUrl,
                    status = "active",
                    createdAt = System.currentTimeMillis()
                )

                Log.d("ListingsViewModel", "Listing object created: $listing")
                Log.d("ListingsViewModel", "Saving to Firestore...")

                // Save to Firestore
                val docRef = firestore.collection("listings")
                    .add(listing)
                    .await()

                Log.d("ListingsViewModel", "Saved to Firestore with ID: ${docRef.id}")

                _successMessage.value = "Item posted successfully!"
                _isLoading.value = false

                Log.d("ListingsViewModel", "Refreshing listings...")

                // Refresh listings
                fetchAllListings()
                fetchMyListings()

            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error creating listing", e)
                Log.e("ListingsViewModel", "Error details: ${e.message}")
                Log.e("ListingsViewModel", "Error stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Failed to create listing: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Select listing for detail view
    fun selectListing(listing: Listing) {
        _selectedListing.value = listing
        Log.d("ListingsViewModel", "Selected listing: ${listing.title}")
    }

    // Mark listing as sold
    fun markAsSold(listingId: String) {
        viewModelScope.launch {
            try {
                Log.d("ListingsViewModel", "Marking listing $listingId as sold")

                firestore.collection("listings")
                    .document(listingId)
                    .update("status", "sold")
                    .await()

                Log.d("ListingsViewModel", "Successfully marked as sold")

                _successMessage.value = "Item marked as sold!"

                // Refresh listings
                fetchAllListings()
                fetchMyListings()

            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error marking as sold", e)
                _errorMessage.value = "Failed to mark as sold: ${e.message}"
            }
        }
    }

    // Clear messages
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}