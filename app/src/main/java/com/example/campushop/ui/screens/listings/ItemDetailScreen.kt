package com.example.campushop.ui.screens.listings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.campushop.data.model.Listing
import com.example.campushop.data.repository.AuthRepository
import com.example.campushop.viewmodel.ListingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    listing: Listing,
    onNavigateBack: () -> Unit,
    onContactSeller: (Listing) -> Unit,
    viewModel: ListingsViewModel = viewModel()
) {
    val authRepository = AuthRepository()
    val currentUserId = authRepository.getCurrentUserId()
    val isOwner = currentUserId == listing.userId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image
            AsyncImage(
                model = listing.imageUrl,
                contentDescription = listing.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // Title
            Text(
                text = listing.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Price
            Text(
                text = "₹${listing.price}",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Category
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Category",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = listing.category)
                }
            }

            // Condition
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Condition",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = listing.condition)
                }
            }

            // Price Type
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Price Type",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = listing.priceType)
                }
            }

            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Description",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = listing.description)
                }
            }

            // Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Status",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = listing.status.uppercase(),
                        color = if (listing.status == "active") 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            // Contact Seller Button
            if (!isOwner && listing.status == "active") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onContactSeller(listing) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat with Seller")
                }
            }
        }
    }
}
