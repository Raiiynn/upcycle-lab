package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.ui.theme.UpCycleTheme
import com.example.upcycle.ui.theme.HunterGreen
import com.example.upcycle.ui.theme.SageGreen
import com.example.upcycle.ui.theme.WarmWhite
import com.example.upcycle.ui.theme.TextDark
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.upcycle.model.UpcycleData

@Composable
fun CreateAccountScreen(
    onSignUpSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val auth = remember { Firebase.auth }
    val db = remember { Firebase.firestore }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {
        // Background decorative elements
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = (-60).dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(SageGreen.copy(alpha = 0.1f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = 100.dp)
                .size(150.dp)
                .clip(CircleShape)
                .background(HunterGreen.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Branding Section
            Surface(
                shape = CircleShape,
                color = HunterGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Eco, contentDescription = "Logo", tint = HunterGreen, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "UpCycle", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, color = HunterGreen, letterSpacing = 1.sp))
            Text(text = "Mulai perjalanan hijaumu.", style = MaterialTheme.typography.bodyMedium, color = TextDark.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(48.dp))

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 16.dp))
            }

            // Registration Form
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = HunterGreen
            )
            val inputShape = RoundedCornerShape(16.dp)

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = HunterGreen) },
                modifier = Modifier.fillMaxWidth(),
                shape = inputShape,
                singleLine = true,
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = HunterGreen) },
                modifier = Modifier.fillMaxWidth(),
                shape = inputShape,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = HunterGreen) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle Password", tint = Color.Gray)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = inputShape,
                singleLine = true,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Action Button
            Button(
                onClick = {
                    if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        errorMessage = null
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = task.result?.user?.uid ?: ""
                                    val userProfile = hashMapOf(
                                        "uid" to uid,
                                        "username" to username,
                                        "email" to email,
                                        "points" to 0,
                                        "level" to "Eco Beginner",
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    db.collection("users").document(uid).set(userProfile)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            UpcycleData.currentUser = username
                                            onSignUpSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = "Gagal simpan profil: ${e.message}"
                                        }
                                } else {
                                    isLoading = false
                                    errorMessage = task.exception?.message ?: "Registrasi gagal"
                                }
                            }
                    } else {
                        errorMessage = "Semua kolom harus diisi"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HunterGreen, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Onboarding Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HunterGreen.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Eco, contentDescription = null, tint = HunterGreen, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Misi Kita Bersama", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HunterGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Ubah sampah jadi berkah. Bergabunglah dengan ribuan pahlawan lingkungan lainnya.", style = MaterialTheme.typography.bodySmall, color = TextDark.copy(alpha = 0.7f), lineHeight = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Login Link
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Sudah punya akun?", style = MaterialTheme.typography.bodyMedium, color = TextDark.copy(alpha = 0.6f))
                TextButton(onClick = onLoginClick) {
                    Text(text = "Masuk", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = HunterGreen)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateAccountScreenPreview() {
    UpCycleTheme {
        CreateAccountScreen()
    }
}