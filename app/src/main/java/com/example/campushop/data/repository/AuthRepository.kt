package com.example.campushop.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<Boolean> {
        return try {
            val email = account.email ?: ""

            // VALIDATION: Check if email is an educational email
            val isEducationalEmail = email.endsWith(".edu.in", ignoreCase = true) ||
                    email.endsWith(".ac.in", ignoreCase = true) ||
                    email.endsWith(".edu", ignoreCase = true)

            if (!isEducationalEmail) {
                return Result.failure(Exception("Please use your university email address (.edu, .edu.in, or .ac.in)"))
            }

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val userId = result.user?.uid ?: throw Exception("Sign in failed")

            val userDoc = firestore.collection("users").document(userId).get().await()

            if (!userDoc.exists()) {
                // New user - create document with profile incomplete
                val userData = hashMapOf(
                    "name" to (account.displayName ?: "Unknown"),
                    "email" to email,
                    "department" to "",
                    "year" to "",
                    "userId" to userId,
                    "createdAt" to System.currentTimeMillis(),
                    "profileComplete" to false
                )
                firestore.collection("users").document(userId).set(userData).await()
                // Return false to indicate profile is incomplete
                return Result.success(false)
            } else {
                // Existing user - check if profile is complete
                val isComplete = userDoc.getBoolean("profileComplete") ?: false
                return Result.success(isComplete)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    suspend fun getUserProfile(userId: String): Result<Map<String, Any>> {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                Result.success(doc.data ?: emptyMap())
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isProfileComplete(userId: String): Boolean {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getBoolean("profileComplete") ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserProfile(userId: String, name: String, department: String, year: String): Result<Unit> {
        return try {
            val updates = hashMapOf(
                "name" to name,
                "department" to department,
                "year" to year,
                "profileComplete" to true
            )
            firestore.collection("users").document(userId).update(updates as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}