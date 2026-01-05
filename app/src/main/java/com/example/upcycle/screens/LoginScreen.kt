package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.ui.theme.UpCycleTheme
import com.example.upcycle.ui.theme.HunterGreen
import com.example.upcycle.ui.theme.SageGreen
import com.example.upcycle.ui.theme.WarmWhite
import com.example.upcycle.ui.theme.TextDark
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val auth = remember { Firebase.auth }
    val db = remember { Firebase.firestore }
    
    val isUsernameValid = username.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // 1. Header
        Surface(
            shape = CircleShape,
            color = HunterGreen.copy(alpha = 0.1f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "Logo",
                    tint = HunterGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Welcome Back!", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, color = TextDark))
        Text(text = "Siap melanjutkan aksimu?", style = MaterialTheme.typography.bodyLarge, color = TextDark.copy(alpha = 0.6f))

        Spacer(modifier = Modifier.height(48.dp))

        // Error Message
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 2. Login Form
        val inputShape = RoundedCornerShape(16.dp)

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = HunterGreen) },
            trailingIcon = { if (isUsernameValid) { Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = HunterGreen) } },
            modifier = Modifier.fillMaxWidth(),
            shape = inputShape,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HunterGreen, unfocusedBorderColor = SageGreen, focusedLabelColor = HunterGreen, cursorColor = HunterGreen)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
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
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HunterGreen, unfocusedBorderColor = SageGreen, focusedLabelColor = HunterGreen, cursorColor = HunterGreen)
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { /* Forgot Password */ }) {
                Text(text = "Lupa Password?", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = HunterGreen)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Main Action Button
        Button(
            onClick = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    errorMessage = null
                    
                    // 1. Query Firestore to find Email by Username
                    db.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                isLoading = false
                                errorMessage = "Username tidak ditemukan."
                            } else {
                                val userEmail = documents.documents[0].getString("email")
                                if (userEmail != null) {
                                    // 2. Sign In with found Email
                                    auth.signInWithEmailAndPassword(userEmail, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val uid = task.result?.user?.uid ?: ""
                                                UpcycleData.syncUserProfile(uid) {
                                                    isLoading = false
                                                    onLoginSuccess()
                                                }
                                            } else {
                                                isLoading = false
                                                errorMessage = "Password salah."
                                            }
                                        }
                                } else {
                                    isLoading = false
                                    errorMessage = "Data akun rusak (email hilang)."
                                }
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = "Gagal memuat data: ${it.message}"
                        }
                } else {
                    errorMessage = "Mohon isi semua kolom."
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HunterGreen),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Log In", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 4. Social Login
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Divider(modifier = Modifier.weight(1f), color = SageGreen.copy(alpha = 0.5f))
            Text(text = "Atau masuk dengan", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = TextDark.copy(alpha = 0.5f))
            Divider(modifier = Modifier.weight(1f), color = SageGreen.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            SocialLoginCircle(text = "G")
            Spacer(modifier = Modifier.width(24.dp))
            SocialLoginCircle(text = "f")
            Spacer(modifier = Modifier.width(24.dp))
            SocialLoginCircle(text = "")
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Belum punya akun?", style = MaterialTheme.typography.bodyMedium, color = TextDark.copy(alpha = 0.6f))
            TextButton(onClick = onSignUpClick) {
                Text(text = "Daftar Sekarang", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = HunterGreen)
            }
        }
    }
}

@Composable
fun SocialLoginCircle(text: String) {
    Surface(
        modifier = Modifier.size(56.dp).clickable { },
        shape = CircleShape,
        color = WarmWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark.copy(alpha = 0.8f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    UpCycleTheme {
        LoginScreen()
    }
}