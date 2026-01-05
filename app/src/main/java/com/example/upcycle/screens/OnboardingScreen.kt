package com.example.upcycle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upcycle.ui.theme.HunterGreen
import com.example.upcycle.ui.theme.UpCycleTheme
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit = {}
) {
    val pages = listOf(
        OnboardingPage(
            title = "Selamat Datang di UpCycle!",
            description = "Ubah sampahmu menjadi karya bernilai. Mari bersama-sama ciptakan masa depan yang lebih hijau.",
            icon = Icons.Default.Eco,
            backgroundColor = HunterGreen
        ),
        OnboardingPage(
            title = "Scan dengan AI",
            description = "Arahkan kamera ke objek sampah, AI kami akan mengidentifikasi dan memberikan rekomendasi ide upcycle.",
            icon = Icons.Default.CameraAlt,
            backgroundColor = Color(0xFF2196F3)
        ),
        OnboardingPage(
            title = "Kelola Gudang & Temukan Ide",
            description = "Simpan sampahmu di gudang virtual dan jelajahi ratusan ide kreatif untuk mendaur ulang.",
            icon = Icons.Default.Inventory2,
            backgroundColor = Color(0xFFFF9800)
        ),
        OnboardingPage(
            title = "Kumpulkan Poin & Badge",
            description = "Selesaikan proyek, dapatkan poin, unlock badges, dan naik level. Bagikan karyamu ke komunitas!",
            icon = Icons.Default.EmojiEvents,
            backgroundColor = Color(0xFF9C27B0)
        )
    )

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Skip Button
        if (pagerState.currentPage < pages.size - 1) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Lewati",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Indicators
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.padding(16.dp),
                activeColor = MaterialTheme.colorScheme.primary,
                inactiveColor = Color.LightGray,
                indicatorWidth = 8.dp,
                indicatorHeight = 8.dp,
                spacing = 8.dp
            )

            // Bottom Button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        // Next page
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        // Finish onboarding
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Lanjut" else "Mulai",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with background
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(page.backgroundColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.backgroundColor,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    UpCycleTheme {
        OnboardingScreen()
    }
}
