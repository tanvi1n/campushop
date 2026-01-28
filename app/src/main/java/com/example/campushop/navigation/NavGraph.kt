package com.example.campushop.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.campushop.data.model.Listing
import com.example.campushop.ui.screens.auth.LoginScreen
import com.example.campushop.ui.screens.auth.RegisterScreen
import com.example.campushop.ui.screens.listings.CreateListingScreen
import com.example.campushop.ui.screens.listings.FeedScreen
import com.example.campushop.ui.screens.listings.ItemDetailScreen
import com.example.campushop.ui.screens.listings.MyListingsScreen
import com.example.campushop.viewmodel.AuthViewModel
import com.example.campushop.viewmodel.ListingsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp

// Screen routes
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Feed : Screen("feed")
    object CreateListing : Screen("create_listing")
    object ItemDetail : Screen("item_detail")
    object MyListings : Screen("my_listings")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    listingsViewModel: ListingsViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Feed Screen (with bottom nav)
        composable(Screen.Feed.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                listingsViewModel = listingsViewModel
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
                    viewModel = listingsViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    listingsViewModel: ListingsViewModel
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
                ProfileScreenPlaceholder(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Feed.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileScreenPlaceholder(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Screen",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}