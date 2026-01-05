package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.model.InventoryItem
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanResultScreen(
    onClose: () -> Unit,
    onIdeaClick: (Int) -> Unit,
    onSaveSuccess: () -> Unit = {}
) {
    var isScanning by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2500) // Simulate scanning delay
        isScanning = false
    }

    if (isScanning) {
        ScanningAnimation()
    } else {
        ScanResultContent(
            onClose = onClose, 
            onIdeaClick = onIdeaClick,
            onSaveSuccess = onSaveSuccess
        )
    }
}

@Composable
fun ScanningAnimation() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Menganalisis Objek...",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "AI sedang mengidentifikasi material",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ScanResultContent(
    onClose: () -> Unit,
    onIdeaClick: (Int) -> Unit,
    onSaveSuccess: () -> Unit
) {
    // Simulated Result: Plastic Bottle
    val detectedItemName = "Botol Plastik (PET)"
    val detectedCategory = "Plastik"
    val detectedWeight = "0.05 Kg"
    val carbonSaved = "150g CO2"
    
    val recommendedIdeas = UpcycleData.ideas.filter { it.category == detectedCategory }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Header Image Simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.DarkGray)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(200.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 16.dp),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 40.dp, bottom = 40.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "98% Match", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // 2. Result Info
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Objek Terdeteksi", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(
                    text = detectedItemName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Potensi Dampak Positif", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                            Text(text = "Hemat $carbonSaved", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Ide Upcycle Rekomendasi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                recommendedIdeas.forEach { idea ->
                    Card(
                        onClick = { onIdeaClick(idea.id) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Thumbnail Image
                            com.example.upcycle.components.UpcycleImage(
                                imageUrl = idea.imageUrl,
                                contentDescription = idea.title,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                                placeholderColor = idea.color.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = idea.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "${idea.difficulty} • ${idea.timeRequired}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { 
                        val newItem = InventoryItem(
                            id = System.currentTimeMillis(),
                            name = detectedItemName,
                            category = detectedCategory,
                            dateAdded = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                            weight = detectedWeight,
                            status = "Perlu Dicuci"
                        )
                        // Save to Firestore via UpcycleData
                        UpcycleData.addInventoryItem(newItem) {
                            onSaveSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // HunterGreen
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Simpan ke Gudang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}