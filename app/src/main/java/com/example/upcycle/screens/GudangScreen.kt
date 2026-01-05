package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.ui.theme.UpCycleTheme
import com.example.upcycle.ui.theme.HunterGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GudangScreen(
    onCategoryClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val inventoryState = UpcycleData.inventory
    
    // Refresh data when screen opens
    LaunchedEffect(Unit) {
        UpcycleData.loadInventory()
    }

    Scaffold(
        // FAB removed
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // 1. GREEN HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HunterGreen)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Gudang Anda",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan barang untuk menambah stok otomatis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search Bar
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = HunterGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text("Cari stok...", color = Color.Gray)
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = Color.Black)
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // 3. Category List
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = UpcycleData.categories,
                        key = { it.name }
                    ) { category ->
                        val itemCount = remember(inventoryState.toList()) {
                            inventoryState.count { it.category == category.name }
                        }
                        
                        InventoryCardSuperApp(
                            name = category.name,
                            count = itemCount,
                            icon = category.icon,
                            color = category.color,
                            onClick = { onCategoryClick(category.name) }
                        )
                    }
                }
            }
        }
    }
}

// REMOVED InputItemContent completely

@Composable
fun InventoryCardSuperApp(
    name: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = "$count Item", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Detail", tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GudangScreenPreview() {
    UpCycleTheme {
        GudangScreen()
    }
}