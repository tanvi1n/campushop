package com.example.campushop.navigation

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campushop.ui.screens.auth.LoginScreen
import com.example.campushop.ui.screens.auth.ProfileSetupScreen
import com.example.campushop.ui.screens.chat.ChatScreen
import com.example.campushop.ui.screens.chat.ConversationsScreen
import com.example.campushop.ui.screens.listings.CreateListingScreen
import com.example.campushop.ui.screens.listings.FeedScreen
import com.example.campushop.ui.screens.listings.ItemDetailScreen
import com.example.campushop.ui.screens.listings.MyListingsScreen
import com.example.campushop.viewmodel.AuthViewModel
import com.example.campushop.viewmodel.ChatViewModel
import com.example.campushop.viewmodel.ListingsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp

// Screen routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ProfileSetup : Screen("profile_setup")
    object Feed : Screen("feed")
    object CreateListing : Screen("create_listing")
    object ItemDetail : Screen("item_detail")
    object MyListings : Screen("my_listings")
    object Profile : Screen("profile")
    object SoldItems : Screen("sold_items")
    object PurchaseHistory : Screen("purchase_history")
    object Conversations : Screen("conversations")
    object Chat : Screen("chat/{conversationId}/{receiverId}/{receiverName}/{listingTitle}/{listingImageUrl}") {
        fun createRoute(
            conversationId: String,
            receiverId: String,
            receiverName: String,
            listingTitle: String,
            listingImageUrl: String?
        ) = "chat/${Uri.encode(conversationId)}/${Uri.encode(receiverId)}/${Uri.encode(receiverName)}/${Uri.encode(listingTitle)}/${Uri.encode(listingImageUrl ?: "null")}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    listingsViewModel: ListingsViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    // Check if user is already logged in
    val startDestination = if (authViewModel.isLoggedIn()) Screen.Feed.route else Screen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen (Google Sign-In only)
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {}, // Can remove this parameter
                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Profile Setup Screen
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onProfileComplete = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Remove Register route if not needed
        // composable(Screen.Register.route) { ... }

        // Feed Screen (with bottom nav)
        composable(Screen.Feed.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                listingsViewModel = listingsViewModel,
                chatViewModel = chatViewModel
            )
        }

        // Create Listing Screen
        composable(Screen.CreateListing.route) {
            CreateListingScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = listingsViewModel
            )
        }

        // Item Detail Screen
        composable(Screen.ItemDetail.route) {
            val selectedListing = listingsViewModel.selectedListing.collectAsState().value
            if (selectedListing != null) {
                ItemDetailScreen(
                    listing = selectedListing,
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = listingsViewModel,
                    onContactSeller = { listing ->
                        chatViewModel.startConversation(
                            listingId = listing.id,
                            listingTitle = listing.title,
                            listingImageUrl = listing.imageUrl,
                            sellerId = listing.sellerId,
                            sellerName = listing.sellerName
                        ) { conversationId ->
                            navController.navigate(
                                Screen.Chat.createRoute(
                                    conversationId = conversationId,
                                    receiverId = listing.sellerId,
                                    receiverName = listing.sellerName,
                                    listingTitle = listing.title,
                                    listingImageUrl = listing.imageUrl
                                )
                            )
                        }
                    }
                )
            }
        }

        // Chat Screen
        composable(Screen.Chat.route) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")?.let { Uri.decode(it) } ?: ""
            val receiverId = backStackEntry.arguments?.getString("receiverId")?.let { Uri.decode(it) } ?: ""
            val receiverName = backStackEntry.arguments?.getString("receiverName")?.let { Uri.decode(it) } ?: ""
            val listingTitle = backStackEntry.arguments?.getString("listingTitle")?.let { Uri.decode(it) } ?: ""
            val listingImageUrl = backStackEntry.arguments?.getString("listingImageUrl")?.let {
                val decoded = Uri.decode(it)
                if (decoded == "null") null else decoded
            }

            ChatScreen(
                conversationId = conversationId,
                receiverId = receiverId,
                receiverName = receiverName,
                listingTitle = listingTitle,
                listingImageUrl = listingImageUrl,
                onNavigateBack = { navController.popBackStack() },
                viewModel = chatViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    listingsViewModel: ListingsViewModel,
    chatViewModel: ChatViewModel
) {
    val localNavController = rememberNavController()
    val navBackStackEntry by localNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed") },
                    selected = currentRoute == "feed_main",
                    onClick = {
                        localNavController.navigate("feed_main") {
                            popUpTo("feed_main") { inclusive = true }
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
                    label = { Text("Sell") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.CreateListing.route)
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Messages") },
                    label = { Text("Messages") },
                    selected = currentRoute == "messages_main",
                    onClick = {
                        localNavController.navigate("messages_main") {
                            popUpTo("feed_main") { inclusive = false }
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "My Items") },
                    label = { Text("My Items") },
                    selected = currentRoute == "my_listings_main",
                    onClick = {
                        localNavController.navigate("my_listings_main") {
                            popUpTo("feed_main") { inclusive = false }
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = currentRoute == "profile_main",
                    onClick = {
                        localNavController.navigate("profile_main") {
                            popUpTo("feed_main") { inclusive = false }
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = localNavController,
            startDestination = "feed_main",
            modifier = Modifier.padding(padding)
        ) {
            composable("feed_main") {
                FeedScreen(
                    onNavigateToDetail = { listing ->
                        listingsViewModel.selectListing(listing)
                        navController.navigate(Screen.ItemDetail.route)
                    },
                    viewModel = listingsViewModel
                )
            }

            composable("messages_main") {
                ConversationsScreen(
                    onNavigateToChat = { conversationId, receiverId, receiverName, listingTitle, listingImageUrl ->
                        navController.navigate(
                            Screen.Chat.createRoute(
                                conversationId = conversationId,
                                receiverId = receiverId,
                                receiverName = receiverName,
                                listingTitle = listingTitle,
                                listingImageUrl = listingImageUrl
                            )
                        )
                    },
                    viewModel = chatViewModel
                )
            }

            composable("my_listings_main") {
                MyListingsScreen(
                    onNavigateToDetail = { listing ->
                        listingsViewModel.selectListing(listing)
                        navController.navigate(Screen.ItemDetail.route)
                    },
                    viewModel = listingsViewModel
                )
            }

            composable("profile_main") {
                com.example.campushop.ui.screens.profile.ProfileScreen(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Feed.route) { inclusive = true }
                        }
                    },
                    onNavigateToSoldItems = {
                        localNavController.navigate(Screen.SoldItems.route)
                    },
                    onNavigateToPurchases = {
                        localNavController.navigate(Screen.PurchaseHistory.route)
                    },
                    viewModel = listingsViewModel
                )
            }

            composable(Screen.SoldItems.route) {
                com.example.campushop.ui.screens.profile.TransactionHistoryScreen(
                    type = "sold",
                    onNavigateBack = { localNavController.popBackStack() },
                    onNavigateToDetail = { listing ->
                        listingsViewModel.selectListing(listing)
                        navController.navigate(Screen.ItemDetail.route)
                    },
                    viewModel = listingsViewModel
                )
            }

            composable(Screen.PurchaseHistory.route) {
                com.example.campushop.ui.screens.profile.TransactionHistoryScreen(
                    type = "purchased",
                    onNavigateBack = { localNavController.popBackStack() },
                    onNavigateToDetail = { listing ->
                        listingsViewModel.selectListing(listing)
                        navController.navigate(Screen.ItemDetail.route)
                    },
                    viewModel = listingsViewModel
                )
            }
        }
    }
}
