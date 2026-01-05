package com.example.upcycle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination

import com.example.upcycle.screens.CreateAccountScreen
import com.example.upcycle.screens.LoginScreen
import com.example.upcycle.screens.ProfileScreen
import com.example.upcycle.screens.HomeScreen
import com.example.upcycle.screens.GudangScreen
import com.example.upcycle.screens.GudangDetailScreen
import com.example.upcycle.screens.ProjectScreen
import com.example.upcycle.screens.ProjectDetailScreen
import com.example.upcycle.screens.ScanScreen
import com.example.upcycle.screens.ScanResultScreen
import com.example.upcycle.screens.IdeaDetailScreen
import com.example.upcycle.screens.CategoryDetailScreen
import com.example.upcycle.screens.CommunityScreen
import com.example.upcycle.screens.CommunityDetailScreen
import com.example.upcycle.screens.CreatePostScreen
import com.example.upcycle.screens.OnboardingScreen
import com.example.upcycle.screens.ActivityHistoryScreen
import com.example.upcycle.ui.theme.UpCycleTheme
import com.example.upcycle.model.UpcycleData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Seed initial data if empty
        UpcycleData.seedInitialData()
        UpcycleData.seedInitialIdeas() // Seed Ideas
        
        enableEdgeToEdge()
        setContent {
            UpCycleTheme { // Fixed typo: UpcycleTheme -> UpCycleTheme
                MainApp(isFirstTime = isFirstTime())
            }
        }
    }
    
    private fun isFirstTime(): Boolean {
        val prefs = getSharedPreferences("upcycle_prefs", MODE_PRIVATE)
        return prefs.getBoolean("is_first_time", true)
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    UpCycleTheme {
        MainApp()
    }
}

@Composable
fun MainApp(isFirstTime: Boolean = true) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Hide bottom bar on onboarding, auth, detail screens, AND PROFILE
            if (currentRoute != "onboarding" &&
                currentRoute != "create_account" && 
                currentRoute != "login" && 
                currentRoute != "scan_result" &&
                currentRoute != "create_post" &&
                currentRoute != "profile" && // Hide on Profile
                currentRoute != "activity_history" && // Hide on Activity History
                currentRoute?.startsWith("idea_detail") != true &&
                currentRoute?.startsWith("category_detail") != true &&
                currentRoute?.startsWith("gudang_detail") != true &&
                currentRoute?.startsWith("project_detail") != true &&
                currentRoute?.startsWith("community_detail") != true
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Gudang,
                        BottomNavItem.Scan,
                        BottomNavItem.Project,
                        BottomNavItem.Community
                    )

                    items.forEach { item ->
                        val isScan = item == BottomNavItem.Scan
                        val isSelected = currentRoute == item.route
                        
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = item.icon, 
                                    contentDescription = item.label,
                                    modifier = if (isScan) Modifier.size(36.dp) else Modifier.size(24.dp),
                                    tint = if (isScan) Color(0xFF2D5A44) else LocalContentColor.current 
                                ) 
                            },
                            label = { Text(item.label) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isScan) Color(0xFF2D5A44) else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isScan) Color(0xFF2D5A44) else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isScan) Color(0xFF2D5A44).copy(alpha = 0.1f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                unselectedIconColor = if (isScan) Color(0xFF2D5A44).copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = if (isFirstTime) "onboarding" else "create_account",
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Onboarding ---
            composable("onboarding") {
                val context = androidx.compose.ui.platform.LocalContext.current
                OnboardingScreen(
                    onFinish = {
                        // Save onboarding completed
                        val prefs = context.getSharedPreferences("upcycle_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("is_first_time", false).apply()
                        
                        navController.navigate("create_account") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            
            // --- Auth Flow ---
            composable("create_account") {
                CreateAccountScreen(
                    onSignUpSuccess = { navController.navigate("login") },
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(BottomNavItem.Home.route) },
                    onSignUpClick = { navController.navigate("create_account") }
                )
            }

            // --- Main Tabs ---
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onProfileClick = { navController.navigate("profile") },
                    onCategoryClick = { categoryId -> navController.navigate("category_detail/$categoryId") },
                    onIdeaClick = { ideaId -> navController.navigate("idea_detail/$ideaId") },
                    onSeeAllCommunityClick = { navController.navigate(BottomNavItem.Community.route) }
                )
            }
            composable(BottomNavItem.Gudang.route) { 
                GudangScreen(
                    onCategoryClick = { categoryName -> navController.navigate("gudang_detail/$categoryName") }
                ) 
            }
            composable(BottomNavItem.Project.route) { 
                ProjectScreen(
                    onProjectClick = { projectId -> navController.navigate("project_detail/$projectId") }
                ) 
            }
            composable(BottomNavItem.Community.route) { 
                CommunityScreen(
                    onPostClick = { postId -> navController.navigate("community_detail/$postId") },
                    onCreatePostClick = { navController.navigate("create_post") }
                ) 
            }
            
            // --- Features ---
            composable(BottomNavItem.Scan.route) {
                ScanScreen(onScanStart = { navController.navigate("scan_result") })
            }
            
            composable("scan_result") {
                ScanResultScreen(
                    onClose = { navController.popBackStack() },
                    onIdeaClick = { ideaId -> navController.navigate("idea_detail/$ideaId") },
                    onSaveSuccess = {
                        navController.navigate(BottomNavItem.Gudang.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable("create_post") {
                CreatePostScreen(
                    onBackClick = { navController.popBackStack() },
                    onPostCreated = {
                        navController.popBackStack()
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onHistoryClick = {
                        navController.navigate("activity_history")
                    }
                )
            }
            
            composable("activity_history") {
                ActivityHistoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- Detail Screens ---
            composable(
                route = "idea_detail/{ideaId}",
                arguments = listOf(navArgument("ideaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val ideaId = backStackEntry.arguments?.getInt("ideaId") ?: 0
                IdeaDetailScreen(
                    ideaId = ideaId,
                    onBackClick = { navController.popBackStack() },
                    onStartProject = {
                        navController.navigate(BottomNavItem.Project.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = "category_detail/{categoryId}",
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                CategoryDetailScreen(
                    categoryId = categoryId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "gudang_detail/{categoryName}",
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                GudangDetailScreen(
                    categoryName = categoryName,
                    onBackClick = { navController.popBackStack() },
                    onIdeaClick = { ideaId -> navController.navigate("idea_detail/$ideaId") }
                )
            }

            composable(
                route = "project_detail/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.IntType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0
                ProjectDetailScreen(
                    projectId = projectId,
                    onBackClick = { navController.popBackStack() },
                    onShareToCommunity = {
                        navController.navigate(BottomNavItem.Community.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = "community_detail/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.IntType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                CommunityDetailScreen(
                    postId = postId,
                    onBackClick = { navController.popBackStack() },
                    onRemakeClick = {
                        navController.navigate(BottomNavItem.Project.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}