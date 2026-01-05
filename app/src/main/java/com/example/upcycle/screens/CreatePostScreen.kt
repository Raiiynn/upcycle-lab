package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.model.CommunityPost
import com.example.upcycle.ui.theme.UpCycleTheme
import com.example.upcycle.ui.theme.HunterGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onBackClick: () -> Unit,
    onPostCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    // Changed steps to a MutableList to manage individual steps
    val stepsList = remember { mutableStateListOf("") } 
    var currentStepInput by remember { mutableStateOf("") }
    
    var difficulty by remember { mutableStateOf("Mudah") }
    var category by remember { mutableStateOf("Plastik") } 

    val difficultyOptions = listOf("Mudah", "Sedang", "Sulit")
    // Added Logam and Tekstil
    val categoryOptions = listOf("Plastik", "Kertas", "Kaca", "Logam", "Tekstil") 

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Ide Baru", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val points = when (difficulty) {
                        "Mudah" -> 50
                        "Sedang" -> 100
                        "Sulit" -> 200
                        else -> 50
                    }
                    
                    // Combine steps into a proper structure (for now dummy text in description, but logic is ready)
                    val stepsFinal = if (currentStepInput.isNotEmpty()) stepsList + currentStepInput else stepsList
                    
                    val newPost = CommunityPost(
                        id = (System.currentTimeMillis() / 1000).toInt(),
                        title = title,
                        creatorName = UpcycleData.currentUser,
                        category = category,
                        impact = "Poin: $points", 
                        likes = 0,
                        color = Color(0xFF8CA993) 
                    )
                    
                    // Save to Firestore via UpcycleData
                    UpcycleData.addCommunityPost(newPost) {
                        onPostCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = title.isNotEmpty() && (stepsList.isNotEmpty() || currentStepInput.isNotEmpty())
            ) {
                Text("Bagikan Ide", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Image Upload
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .clickable { /* TODO */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tambah Foto Karya", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Proyek") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Category Selection (Updated)
            Text("Kategori Bahan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categoryOptions) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Difficulty Selection
            Text("Tingkat Kesulitan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                difficultyOptions.forEach { diff ->
                    FilterChip(
                        selected = difficulty == diff,
                        onClick = { difficulty = diff },
                        label = { Text(diff) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Steps Input (Step-by-Step Builder)
            Text("Langkah Pengerjaan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Tambahkan langkah satu per satu agar mudah diikuti.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))

            // List of added steps
            stepsList.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(step, modifier = Modifier.weight(1f))
                    IconButton(onClick = { stepsList.removeAt(index) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color.Gray)
                    }
                }
            }

            // Input for new step
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = currentStepInput,
                    onValueChange = { currentStepInput = it },
                    placeholder = { Text("Tulis langkah selanjutnya...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (currentStepInput.isNotEmpty()) {
                            stepsList.add(currentStepInput)
                            currentStepInput = ""
                        }
                    })
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (currentStepInput.isNotEmpty()) {
                            stepsList.add(currentStepInput)
                            currentStepInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostScreenPreview() {
    UpCycleTheme {
        CreatePostScreen(onBackClick = {}, onPostCreated = {})
    }
}