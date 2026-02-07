package com.example.campushop.ui.screens.listings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.campushop.data.model.Listing
import com.example.campushop.viewmodel.ListingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    onNavigateToDetail: (Listing) -> Unit,
    viewModel: ListingsViewModel = viewModel()
) {
    val myListings by viewModel.myListings.collectAsState()
    var showBuyerDialog by remember { mutableStateOf(false) }
    var selectedListingId by remember { mutableStateOf("") }
    var buyerEmail by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    if (showBuyerDialog) {
        AlertDialog(
            onDismissRequest = { 
                showBuyerDialog = false
                errorMessage = ""
            },
            title = { Text("Mark as Sold") },
            text = {
                Column {
                    Text("Enter buyer's email address:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = buyerEmail,
                        onValueChange = { buyerEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        isError = errorMessage.isNotEmpty()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (buyerEmail.isBlank()) {
                            errorMessage = "Email is required"
                        } else {
                            viewModel.markAsSoldWithEmail(selectedListingId, buyerEmail) { success, error ->
                                if (success) {
                                    showBuyerDialog = false
                                    buyerEmail = ""
                                    errorMessage = ""
                                } else {
                                    errorMessage = error ?: "User not found"
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showBuyerDialog = false
                    buyerEmail = ""
                    errorMessage = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Listings") }
            )
        }
    ) { padding ->

        if (myListings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You haven't posted any items yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(myListings) { listing ->
                    MyListingCard(
                        listing = listing,
                        onClick = { onNavigateToDetail(listing) },
                        onMarkAsSold = { listingId -> 
                            selectedListingId = listingId
                            showBuyerDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyListingCard(
    listing: Listing,
    onClick: () -> Unit,
    onMarkAsSold: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            AsyncImage(
                model = listing.imageUrl,
                contentDescription = listing.title,
                modifier = Modifier
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = listing.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "₹${listing.price}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status badge
                Card(
                    colors = if (listing.status == "active") {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    }
                ) {
                    Text(
                        text = listing.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mark as Sold button
            if (listing.status == "active") {
                Button(
                    onClick = { onMarkAsSold(listing.listingId) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Mark Sold")
                }
            }
        }
    }
}
