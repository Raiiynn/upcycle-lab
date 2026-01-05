package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.ui.theme.UpCycleTheme
import com.example.upcycle.model.ProjectItem
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.ui.theme.HunterGreen

@Composable
fun ProjectScreen(onProjectClick: (Int) -> Unit = {}) {
    // Use centralized data
    val projects = UpcycleData.projects
    
    // Load Projects on Launch
    LaunchedEffect(Unit) {
        UpcycleData.loadProjects()
    }

    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Belum Dimulai", "Berjalan", "Selesai")

    val filteredProjects = if (selectedFilter == "Semua") {
        projects
    } else {
        projects.filter { it.status == selectedFilter }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // 1. GREEN HEADER (Custom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HunterGreen, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Proyek Saya",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Lanjutkan karyamu demi bumi yang lebih baik.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Project List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredProjects) { project ->
                    ProjectCard(project = project, onClick = { onProjectClick(project.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCard(project: ProjectItem, onClick: () -> Unit = {}) {
    val isCompleted = project.progress >= 1.0f || project.status == "Selesai"
    val isNotStarted = project.status == "Belum Dimulai"

    // Standard Tri-State Colors
    val (themePrimaryColor, themeBackgroundColor, statusIcon) = when {
        isCompleted -> Triple(
            Color(0xFF388E3C), // Green
            Color(0xFFE8F5E9),
            Icons.Default.CheckCircle
        )
        isNotStarted -> Triple(
            Color(0xFFD32F2F), // Red
            Color(0xFFFFEBEE),
            Icons.Default.Warning
        )
        else -> Triple(
            Color(0xFFF57C00), // Orange
            Color(0xFFFFF3E0),
            Icons.Default.PlayArrow
        )
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Visual Placeholder with Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                com.example.upcycle.components.UpcycleImage(
                    imageUrl = project.imageUrl,
                    contentDescription = project.title,
                    modifier = Modifier.fillMaxSize(),
                    placeholderColor = themeBackgroundColor
                )
                
                // Category label overlay
                Text(
                    text = project.category.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = themePrimaryColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(24.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        val statusText = when {
                            isCompleted -> "Selesai"
                            isNotStarted -> "Belum Dimulai"
                            else -> "Proses: ${(project.progress * 100).toInt()}%"
                        }
                        
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = themePrimaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                if (!isNotStarted) {
                    LinearProgressIndicator(
                        progress = { project.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = themePrimaryColor,
                        trackColor = themeBackgroundColor,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text(
                        text = "Klik untuk mulai mengerjakan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Smart Stock Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompleted) {
                        Text(
                            text = "Proyek Berhasil Diselesaikan!",
                            style = MaterialTheme.typography.labelMedium,
                            color = themePrimaryColor
                        )
                    } else if (project.isMaterialReady) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bahan Tersedia di Gudang",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Stok Bahan Kurang",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectScreenPreview() {
    UpCycleTheme {
        ProjectScreen()
    }
}