package com.example.campushop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushop.data.model.Conversation
import com.example.campushop.data.model.Message
import com.example.campushop.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Start or get existing conversation
    fun startConversation(
        listingId: String,
        listingTitle: String,
        listingImageUrl: String?,
        sellerId: String,
        sellerName: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getOrCreateConversation(
                listingId, listingTitle, listingImageUrl, sellerId, sellerName
            )
            _isLoading.value = false

            result.onSuccess { conversationId ->
                onSuccess(conversationId)
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    // Load messages for a conversation
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            repository.getMessages(conversationId).collect { messageList ->
                _messages.value = messageList
            }
        }
    }

    // Send a message
    fun sendMessage(
        conversationId: String,
        receiverId: String,
        messageText: String,
        imageUrl: String? = null
    ) {
        if (messageText.isBlank() && imageUrl == null) return

        viewModelScope.launch {
            val result = repository.sendMessage(conversationId, receiverId, messageText, imageUrl)
            result.onFailure { e ->
                _error.value = e.message
            }
        }
    }

    // Load all conversations
    fun loadConversations() {
        viewModelScope.launch {
            repository.getUserConversations().collect { convList ->
                _conversations.value = convList
            }
        }
    }

    // Load seller conversations
    fun loadSellerConversations() {
        viewModelScope.launch {
            repository.getSellerConversations().collect { convList ->
                _conversations.value = _conversations.value + convList
            }
        }
    }

    // Mark messages as read
    fun markAsRead(conversationId: String) {
        viewModelScope.launch {
            repository.markMessagesAsRead(conversationId)
        }
    }

    fun clearError() {
        _error.value = null
    }
}