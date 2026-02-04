package com.example.campushop.data.repository

import com.example.campushop.data.model.Conversation
import com.example.campushop.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get or create conversation ID
    suspend fun getOrCreateConversation(
        listingId: String,
        listingTitle: String,
        listingImageUrl: String?,
        sellerId: String,
        sellerName: String
    ): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val currentUserName = auth.currentUser?.displayName ?: "Anonymous"

            // Create conversation ID: format "listing_buyer_seller"
            val conversationId = "${listingId}_${currentUserId}_$sellerId"

            // Check if conversation already exists
            val existingConv = firestore.collection("conversations")
                .document(conversationId)
                .get()
                .await()

            if (!existingConv.exists()) {
                // Create new conversation
                val conversation = Conversation(
                    id = conversationId,
                    listingId = listingId,
                    listingTitle = listingTitle,
                    listingImageUrl = listingImageUrl,
                    buyerId = currentUserId,
                    buyerName = currentUserName,
                    sellerId = sellerId,
                    sellerName = sellerName,
                    lastMessage = "",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    unreadCount = 0
                )

                firestore.collection("conversations")
                    .document(conversationId)
                    .set(conversation)
                    .await()
            }

            Result.success(conversationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Send a message
    suspend fun sendMessage(
        conversationId: String,
        receiverId: String,
        messageText: String,
        imageUrl: String? = null
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val currentUserName = auth.currentUser?.displayName ?: "Anonymous"

            val messageId = firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document().id

            val message = Message(
                id = messageId,
                conversationId = conversationId,
                senderId = currentUserId,
                senderName = currentUserName,
                receiverId = receiverId,
                message = messageText,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            // Add message to subcollection
            firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .await()

            // Update conversation's last message
            firestore.collection("conversations")
                .document(conversationId)
                .update(
                    mapOf(
                        "lastMessage" to messageText,
                        "lastMessageTimestamp" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Listen to messages in real-time
    fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val subscription = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { subscription.remove() }
    }

    // Get all conversations for current user
    fun getUserConversations(): Flow<List<Conversation>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: run {
            close(Exception("Not authenticated"))
            return@callbackFlow
        }

        val subscription = firestore.collection("conversations")
            .whereEqualTo("buyerId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)
                } ?: emptyList()

                trySend(conversations)
            }

        awaitClose { subscription.remove() }
    }

    // Get seller conversations
    fun getSellerConversations(): Flow<List<Conversation>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: run {
            close(Exception("Not authenticated"))
            return@callbackFlow
        }

        val subscription = firestore.collection("conversations")
            .whereEqualTo("sellerId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)
                } ?: emptyList()

                trySend(conversations)
            }

        awaitClose { subscription.remove() }
    }

    // Mark messages as read
    suspend fun markMessagesAsRead(conversationId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))

            val messages = firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            messages.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}