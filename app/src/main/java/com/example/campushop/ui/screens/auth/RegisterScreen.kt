package com.example.campushop.ui.screens.auth


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushop.ui.components.CustomButton
import com.example.campushop.ui.components.CustomTextField
import com.example.campushop.viewmodel.AuthState
import com.example.campushop.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onRegisterSuccess()
                viewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Join the student marketplace",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name field
        CustomTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Email field
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "College Email",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Password field
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password (min 6 characters)",
            isPassword = true,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Department field
        CustomTextField(
            value = department,
            onValueChange = { department = it },
            label = "Department (e.g., CSE)",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Year field
        CustomTextField(
            value = year,
            onValueChange = { year = it },
            label = "Year (e.g., 2nd)",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Error message
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Register button
        CustomButton(
            text = "Register",
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() &&
                    password.length >= 6 && department.isNotBlank() && year.isNotBlank()) {
                    viewModel.register(email, password, name, department, year)
                }
            },
            isLoading = authState is AuthState.Loading,
            enabled = name.isNotBlank() && email.isNotBlank() &&
                    password.length >= 6 && department.isNotBlank() && year.isNotBlank()
        )

        // Login link
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}