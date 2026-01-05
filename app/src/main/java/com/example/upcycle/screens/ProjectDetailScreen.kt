package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.model.ProjectItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Int,
    onBackClick: () -> Unit,
    onShareToCommunity: () -> Unit = {}
) {
    val projectIndex = UpcycleData.projects.indexOfFirst { it.id == projectId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    if (projectIndex == -1) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Proyek tidak ditemukan")
        }
        return
    }

    val initialProject = UpcycleData.projects[projectIndex]
    
    val initialCheckedCount = (initialProject.progress * initialProject.steps.size).toInt()
    
    val stepsState = remember { 
        mutableStateListOf<Boolean>().apply {
            initialProject.steps.forEachIndexed { index, _ ->
                add(index < initialCheckedCount)
            }
        }
    }

    val currentProgress by remember {
        derivedStateOf {
            val checkedCount = stepsState.count { it }
            if (stepsState.isNotEmpty()) checkedCount.toFloat() / stepsState.size else 0f
        }
    }

    // Save Function
    fun saveProgress() {
        val isFinishing = currentProgress >= 1.0f
        val newStatus = if (isFinishing) "Selesai" else "Berjalan"
        
        val updatedProject = initialProject.copy(
            status = newStatus,
            progress = currentProgress
        )
        
        // Update Local & Global
        UpcycleData.projects[projectIndex] = updatedProject
        // Sync to Firestore
        UpcycleData.updateProjectProgress(updatedProject)

        // Points Logic
        if (isFinishing && initialProject.status != "Selesai") {
            // Only add points if finishing for the first time
            val newTotalPoints = UpcycleData.userPoints + 100
            UpcycleData.updateUserPoints(newTotalPoints) // Sync to Firestore
            scope.launch {
                snackbarHostState.showSnackbar("Selamat! +100 Poin ditambahkan ke profilmu.")
            }
        }
    }

    Scaffold(
        // SnackbarHost removed from here
        topBar = {
            TopAppBar(
                title = { Text(initialProject.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Same as before)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(initialProject.color)
                ) {
                    // ... (Header Content remains same)
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = initialProject.impact,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // ... (Body content same as before)
                    val currentGlobalStatus = UpcycleData.projects[projectIndex].status
                    
                    Text(
                        text = "Status: $currentGlobalStatus",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (currentGlobalStatus != "Selesai") {
                        InventoryCheckCard(isReady = initialProject.isMaterialReady)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    when {
                        currentGlobalStatus == "Selesai" -> {
                            CompletedContent(
                                project = initialProject,
                                onShareClick = {
                                    UpcycleData.addPostFromProject(initialProject)
                                    onShareToCommunity()
                                },
                                onBackToProjectClick = onBackClick 
                            )
                        }
                        initialProject.status == "Belum Dimulai" && currentProgress == 0f -> {
                            PreStartContent(
                                onStartClick = {
                                    UpcycleData.projects[projectIndex] = initialProject.copy(status = "Berjalan", progress = 0.05f)
                                }
                            )
                        }
                        else -> {
                            InProgressContent(
                                steps = initialProject.steps,
                                stepsState = stepsState,
                                progress = currentProgress,
                                onStepChanged = { index, isChecked ->
                                    stepsState[index] = isChecked
                                },
                                onSaveClick = { saveProgress() }
                            )
                        }
                    }
                }
            }
            
            // Snackbar Host at Top
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            )
        }
    }
}

// ... (InventoryCheckCard, PreStartContent, InProgressContent remain same)
@Composable
fun InventoryCheckCard(isReady: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isReady) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isReady) Icons.Default.Inventory else Icons.Default.Warning, contentDescription = null, tint = if (isReady) Color(0xFF2E8B57) else Color(0xFFEF6C00))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(if (isReady) "Bahan Tersedia" else "Bahan Kurang", fontWeight = FontWeight.Bold, color = if (isReady) Color(0xFF2E8B57) else Color(0xFFEF6C00))
                Text(if (isReady) "Siap untuk memulai!" else "Cek gudangmu dulu.", style = MaterialTheme.typography.bodySmall, color = Color.Black.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun PreStartContent(onStartClick: () -> Unit) {
    Column {
        Text("Persiapan Awal", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Sebelum memulai, pastikan area kerjamu bersih dan semua alat sudah siap.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
            Text("Mulai Kerjakan")
        }
    }
}

@Composable
fun InProgressContent(
    steps: List<String>,
    stepsState: MutableList<Boolean>,
    progress: Float,
    onStepChanged: (Int, Boolean) -> Unit,
    onSaveClick: () -> Unit
) {
    val isFinishable = progress >= 1.0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Langkah Pengerjaan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
        )
        Spacer(modifier = Modifier.height(24.dp))

        steps.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 8.dp)) {
                Checkbox(checked = stepsState[index], onCheckedChange = { isChecked -> onStepChanged(index, isChecked) })
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("Langkah ${index + 1}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(step, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(if (isFinishable) Icons.Default.CheckCircle else Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isFinishable) "Selesaikan Proyek" else "Simpan Progress")
        }
    }
}

@Composable
fun CompletedContent(
    project: ProjectItem, 
    onShareClick: () -> Unit,
    onBackToProjectClick: () -> Unit // New callback
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Selamat!", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        Text("Kamu telah menyelesaikan proyek ini.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Share Card
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Bagikan Karyamu", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray.copy(alpha = 0.3f)).border(1.dp, Color.Gray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onShareClick, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan ke Komunitas")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Back to Project List Button
        OutlinedButton(
            onClick = onBackToProjectClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.List, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali ke Daftar Proyek")
        }
    }
}