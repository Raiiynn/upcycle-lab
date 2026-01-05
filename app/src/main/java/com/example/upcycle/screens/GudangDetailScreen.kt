package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.model.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GudangDetailScreen(
    categoryName: String,
    onBackClick: () -> Unit,
    onIdeaClick: (Int) -> Unit
) {
    val category = UpcycleData.categories.find { it.name == categoryName } ?: UpcycleData.categories.first()
    
    // Filter Global Inventory by Category
    // Use derivedStateOf if complex, but direct access works for simple mutable list
    val inventoryItems = UpcycleData.inventory.filter { it.category == categoryName }

    val recommendedIdeas = UpcycleData.ideas.filter { it.category == categoryName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add specific item via FAB in detail screen if needed */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Item")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Statistics Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = category.color),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatColumn(value = "---", label = "Total Berat", icon = Icons.Default.Scale) // Placeholder
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.White.copy(alpha = 0.5f))
                        StatColumn(value = "${inventoryItems.size} Item", label = "Total Barang", icon = Icons.Default.Delete)
                    }
                }
            }

            // 2. Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.Gray)
                    SuggestionChip(onClick = {}, label = { Text("Semua") })
                    SuggestionChip(onClick = {}, label = { Text("Terbaru") })
                    SuggestionChip(onClick = {}, label = { Text("Terberat") })
                }
            }

            // 3. Inventory List
            item {
                Text(
                    text = "Daftar Inventaris",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(inventoryItems) { item ->
                InventoryDetailCard(item)
            }

            // 4. Smart Recommendations
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rekomendasi Proyek",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Berdasarkan stok ${categoryName} kamu",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            items(recommendedIdeas) { idea ->
                IdeaRecommendationCard(
                    title = idea.title,
                    difficulty = idea.difficulty,
                    color = idea.color,
                    onClick = { onIdeaClick(idea.id) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(60.dp)) // Space for FAB
            }
        }
    }
}

@Composable
fun StatColumn(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun InventoryDetailCard(item: InventoryItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder letter
                Text(item.name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = item.dateAdded, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "•  ${item.status}", style = MaterialTheme.typography.bodySmall, color = if(item.status == "Bersih") Color(0xFF2E8B57) else Color(0xFFD68C45))
                }
            }
            
            Text(text = item.weight, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun IdeaRecommendationCard(title: String, difficulty: String, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = "Tingkat: $difficulty", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}