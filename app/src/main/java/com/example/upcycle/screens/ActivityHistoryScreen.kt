package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.upcycle.model.ActivityEvent
import com.example.upcycle.model.ActivityType
import com.example.upcycle.model.UpcycleData
import com.example.upcycle.ui.theme.UpCycleTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf<ActivityType?>(null) }
    
    val activities = UpcycleData.activityHistory
    
    // Refresh data when screen opens
    LaunchedEffect(Unit) {
        UpcycleData.loadActivityHistory()
    }
    
    // Filter activities based on selected type
    val filteredActivities = if (selectedFilter == null) {
        activities
    } else {
        activities.filter { it.type == selectedFilter }
    }
    
    // Group by date
    val groupedActivities = groupActivitiesByDate(filteredActivities)
    
    // Calculate stats
    val thisMonthCount = activities.count { isThisMonth(it.timestamp) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Riwayat Aktivitas",
                        fontWeight = FontWeight.Bold
                    ) 
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Stats Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        icon = Icons.Default.CalendarMonth,
                        value = "$thisMonthCount",
                        label = "Bulan Ini"
                    )
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                    StatItem(
                        icon = Icons.Default.TrendingUp,
                        value = "${activities.size}",
                        label = "Total"
                    )
                }
            }
            
            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = if (selectedFilter == null) 0 else ActivityType.values().indexOf(selectedFilter) + 1,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    text = { Text("Semua") }
                )
                
                ActivityType.values().forEach { type ->
                    Tab(
                        selected = selectedFilter == type,
                        onClick = { selectedFilter = type },
                        text = { Text(getTypeLabel(type)) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Timeline List
            if (filteredActivities.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    groupedActivities.forEach { (dateLabel, events) ->
                        item {
                            DateHeader(dateLabel)
                        }
                        
                        items(events) { event ->
                            TimelineItem(
                                event = event,
                                isLast = event == events.last() && dateLabel == groupedActivities.keys.last()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DateHeader(dateLabel: String) {
    Text(
        text = dateLabel,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun TimelineItem(event: ActivityEvent, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 16.dp)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(event.getColor().copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.getIcon(),
                    contentDescription = null,
                    tint = event.getColor(),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Timeline line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            if (event.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(event.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.List,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum Ada Aktivitas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mulai scan barang atau buat proyek untuk melihat riwayat aktivitasmu",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// Helper Functions

fun groupActivitiesByDate(events: List<ActivityEvent>): Map<String, List<ActivityEvent>> {
    val now = System.currentTimeMillis()
    val grouped = linkedMapOf<String, MutableList<ActivityEvent>>()
    
    events.forEach { event ->
        val label = when {
            isToday(event.timestamp, now) -> "Hari Ini"
            isYesterday(event.timestamp, now) -> "Kemarin"
            isThisWeek(event.timestamp, now) -> "Minggu Ini"
            else -> formatDate(event.timestamp)
        }
        
        grouped.getOrPut(label) { mutableListOf() }.add(event)
    }
    
    return grouped
}

fun isToday(timestamp: Long, now: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
    val cal2 = Calendar.getInstance().apply { timeInMillis = now }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(timestamp: Long, now: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
    val cal2 = Calendar.getInstance().apply { timeInMillis = now - 86400000 } // Yesterday
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isThisWeek(timestamp: Long, now: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
    val cal2 = Calendar.getInstance().apply { timeInMillis = now }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}

fun isThisMonth(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
    val cal2 = Calendar.getInstance()
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Baru saja"
        diff < 3600000 -> "${diff / 60000} menit lalu"
        diff < 86400000 -> "${diff / 3600000} jam lalu"
        diff < 172800000 -> "Kemarin"
        else -> formatDate(timestamp)
    }
}

fun getTypeLabel(type: ActivityType): String = when (type) {
    ActivityType.SCAN -> "Scan"
    ActivityType.PROJECT_START -> "Mulai Proyek"
    ActivityType.PROJECT_COMPLETE -> "Selesai"
    ActivityType.COMMUNITY_POST -> "Posting"
    ActivityType.COMMUNITY_LIKE -> "Like"
    ActivityType.BADGE_UNLOCK -> "Badge"
}

@Preview(showBackground = true)
@Composable
fun ActivityHistoryScreenPreview() {
    UpCycleTheme {
        ActivityHistoryScreen()
    }
}
