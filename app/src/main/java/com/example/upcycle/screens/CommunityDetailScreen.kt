package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.model.CommunityPost
import com.example.upcycle.ui.theme.UpCycleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
    postId: Int,
    onBackClick: () -> Unit,
    onRemakeClick: () -> Unit = {}
) {
    // Simulate getting data
    val post = UpcycleData.posts.find { it.id == postId } ?: UpcycleData.posts.first()
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likes) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Karya Komunitas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Action Bar for Social Interaction
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like Button
                    IconButton(
                        onClick = { 
                            isLiked = !isLiked
                            likesCount = if (isLiked) likesCount + 1 else likesCount - 1
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                    }
                    
                    // Comment Placeholder
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("12 Komentar", style = MaterialTheme.typography.bodyMedium)
                    }

                    // Share Button
                    IconButton(onClick = { /* Share Logic */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Gray)
                    }
                    
                    // Remake Button (CTA)
                    Button(
                        onClick = { 
                            UpcycleData.addProjectFromCommunity(post)
                            onRemakeClick()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Coba Buat")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Hero Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(post.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.title.take(1),
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // 2. Creator & Title Info
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar Placeholder
                        Surface(
                            shape = CircleShape,
                            color = Color.LightGray,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = post.creatorName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Eco Warrior • 2 Jam yang lalu",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { /* Follow Logic */ }) {
                            Text("Ikuti", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Impact Badge
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Recycling, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Menyelamatkan: ${post.impact}",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 3. Description & Materials
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Deskripsi Karya",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saya membuat ${post.title} ini dari bahan ${post.category} bekas yang saya kumpulkan selama seminggu. Prosesnya cukup mudah dan hasilnya sangat memuaskan!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Bahan Utama",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Simple material tag
                    SuggestionChip(
                        onClick = {},
                        label = { Text(post.category) }
                    )
                }
            }

            // 4. Comments Section (Simulated)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 8.dp)
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Komentar (2)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CommentItem("Budi_Santoso", "Keren banget! Mau coba bikin juga ah.")
                    Spacer(modifier = Modifier.height(12.dp))
                    CommentItem("Siti_Aminah", "Warnanya bagus, kreatif sekali idenya!")
                }
                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
            }
        }
    }
}

@Composable
fun CommentItem(user: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(shape = CircleShape, color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(user.take(1), fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(user, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityDetailScreenPreview() {
    UpCycleTheme {
        CommunityDetailScreen(postId = 1, onBackClick = {})
    }
}