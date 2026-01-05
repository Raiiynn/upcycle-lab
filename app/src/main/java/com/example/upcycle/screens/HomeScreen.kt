package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.ui.theme.UpCycleTheme
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.model.UpcycleIdea
import com.example.upcycle.model.CommunityPost
import com.example.upcycle.ui.theme.HunterGreen

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onIdeaClick: (Int) -> Unit = {},
    onSeeAllCommunityClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Use derivedStateOf to observe changes in the list content efficiently
    val filteredIdeas by remember {
        derivedStateOf {
            UpcycleData.ideas.filter { idea ->
                // 1. Text Search Logic
                val searchKeywords = searchQuery.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                
                val matchesSearch = if (searchKeywords.isEmpty()) true else {
                    searchKeywords.any { keyword ->
                        idea.title.contains(keyword, ignoreCase = true) ||
                        idea.category.contains(keyword, ignoreCase = true) ||
                        idea.materials.any { material -> material.contains(keyword, ignoreCase = true) }
                    }
                }

                // 2. Filter Logic (Single Select)
                val matchesFilter = if (selectedCategory == null) true else {
                    idea.category == selectedCategory
                }

                matchesSearch && matchesFilter
            }
        }
    }
    
    val topCommunityPosts by remember {
        derivedStateOf {
            UpcycleData.posts.sortedByDescending { it.likes }.take(3)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        
        // 1. 3D-Style Hero Header
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                color = HunterGreen,
                shadowElevation = 8.dp,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 32.dp)
                ) {
                    // Greeting & Profile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Halo, ${UpcycleData.currentUser}!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Siap menyelamatkan bumi?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .size(52.dp)
                                .clickable { onProfileClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Floating Search Bar
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        shadowElevation = 12.dp,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = HunterGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text("Cari: Botol Plastik, Kardus...", color = Color.Gray)
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // 3. Quick Filter (CENTERED)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Filter Cepat", 
                    style = MaterialTheme.typography.labelLarge, 
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Using LazyRow with centering logic
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(UpcycleData.categories) { category ->
                        val isSelected = selectedCategory == category.name
                        QuickFilterIcon(
                            categoryName = category.name,
                            icon = category.icon,
                            color = category.color,
                            isSelected = isSelected,
                            onClick = {
                                selectedCategory = if (isSelected) null else category.name
                            }
                        )
                    }
                }
            }
        }

        // 4. Featured Ideas Header (CENTERED with GREEN CARD)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HunterGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ide Upcycle Pilihan", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White // White text on green
                    )
                    if (searchQuery.isNotEmpty() || selectedCategory != null) {
                        Text(
                            text = "${filteredIdeas.size} Hasil ditemukan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (filteredIdeas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada ide yang cocok.", color = Color.Gray)
                }
            }
        } else {
            items(filteredIdeas.take(5)) { idea ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    IdeaCardWithTags(idea = idea, onClick = { onIdeaClick(idea.id) })
                }
            }
        }

        // 6. Community Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Inspirasi Komunitas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = "Lihat Semua",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSeeAllCommunityClick() }
                )
            }
        }

        // 7. Community List
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(topCommunityPosts) { post ->
                    CommunityItemCard(post)
                }
            }
        }
    }
}

// --- Sub-Composables ---

@Composable
fun QuickFilterIcon(
    categoryName: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            // LOGIC: Selected = Full Color. Unselected = Light Tint.
            color = if (isSelected) color else color.copy(alpha = 0.1f),
            modifier = Modifier
                .size(56.dp)
                .clickable { onClick() },
            shadowElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = categoryName,
                    // LOGIC: Selected = White Icon. Unselected = Color Icon.
                    tint = if (isSelected) Color.White else color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = categoryName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun IdeaCardWithTags(idea: UpcycleIdea, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image
            com.example.upcycle.components.UpcycleImage(
                imageUrl = idea.imageUrl,
                contentDescription = idea.title,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholderColor = idea.color.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = idea.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Smart Tags Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SignalCellularAlt, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = idea.difficulty, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = idea.timeRequired, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityItemCard(post: CommunityPost) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image
            com.example.upcycle.components.UpcycleImage(
                imageUrl = post.imageUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholderColor = post.color.copy(alpha = 0.5f)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
                Text(
                    text = "oleh @${post.creatorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    UpCycleTheme {
        HomeScreen()
    }
}