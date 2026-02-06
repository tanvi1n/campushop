package com.example.campushop.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campushop.viewmodel.AuthState
import com.example.campushop.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileComplete: () -> Unit,
    viewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("CSE") }
    var year by remember { mutableStateOf("1st") }
    var showDepartmentMenu by remember { mutableStateOf(false) }
    var showYearMenu by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()

    val departments = listOf("CSE", "ECE", "EEE", "AIML", "IT")
    val years = listOf("1st", "2nd", "3rd", "4th")

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onProfileComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Complete Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = rollNumber,
            onValueChange = { rollNumber = it },
            label = { Text("Roll Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = showDepartmentMenu,
            onExpandedChange = { showDepartmentMenu = it }
        ) {
            OutlinedTextField(
                value = department,
                onValueChange = {},
                readOnly = true,
                label = { Text("Department") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDepartmentMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = showDepartmentMenu,
                onDismissRequest = { showDepartmentMenu = false }
            ) {
                departments.forEach { dept ->
                    DropdownMenuItem(
                        text = { Text(dept) },
                        onClick = {
                            department = dept
                            showDepartmentMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = showYearMenu,
            onExpandedChange = { showYearMenu = it }
        ) {
            OutlinedTextField(
                value = year,
                onValueChange = {},
                readOnly = true,
                label = { Text("Year") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showYearMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = showYearMenu,
                onDismissRequest = { showYearMenu = false }
            ) {
                years.forEach { yr ->
                    DropdownMenuItem(
                        text = { Text(yr) },
                        onClick = {
                            year = yr
                            showYearMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.updateProfile(name, rollNumber, department, year) },
            enabled = name.isNotBlank() && rollNumber.isNotBlank() && authState !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Complete Profile")
            }
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
