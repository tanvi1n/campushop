package com.example.campushop.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushop.data.repository.AuthRepository
import com.example.campushop.viewmodel.ListingsViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToSoldItems: () -> Unit = {},
    onNavigateToPurchases: () -> Unit = {},
    viewModel: ListingsViewModel = viewModel()
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val soldItems by viewModel.soldItems.collectAsState()
    val purchasedItems by viewModel.purchasedItems.collectAsState()

    LaunchedEffect(Unit) {
        scope.launch {
            val userId = authRepository.getCurrentUserId()
            email = authRepository.getCurrentUserEmail() ?: ""
            
            if (userId != null) {
                val result = authRepository.getUserProfile(userId)
                result.onSuccess { profile ->
                    name = profile["name"] as? String ?: ""
                    department = profile["department"] as? String ?: ""
                    year = profile["year"] as? String ?: ""
                }
            }
            
            // Fetch sold and purchased items
            viewModel.fetchSoldItems()
            viewModel.fetchPurchasedItems()
            
            isLoading = false
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            authRepository.deleteAccount()
                            onLogout()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Icon
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Info Cards
            ProfileInfoCard(
                icon = Icons.Default.Email,
                label = "Email",
                value = email
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoCard(
                icon = Icons.Default.School,
                label = "Department",
                value = department
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoCard(
                icon = Icons.Default.School,
                label = "Year",
                value = year
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoCard(
                icon = Icons.Default.Sell,
                label = "Sold Items",
                value = "${soldItems.size}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoCard(
                icon = Icons.Default.ShoppingBag,
                label = "Purchased Items",
                value = "${purchasedItems.size}"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Transaction History Buttons
            OutlinedButton(
                onClick = onNavigateToSoldItems,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Sell, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Sold Items")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToPurchases,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Purchase History")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Delete Account")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
