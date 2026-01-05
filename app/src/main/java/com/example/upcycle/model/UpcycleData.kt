package com.example.upcycle.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// --- Data Models ---

data class UpcycleIdea(
    val id: Int,
    val title: String,
    val difficulty: String, 
    val timeRequired: String,
    val description: String,
    val tools: List<String>,
    val materials: List<String>,
    val steps: List<String>,
    val category: String,
    val color: Color,
    val imageUrl: String = ""
)

data class WasteCategory(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val types: List<WasteType>,
    val recyclingTips: List<String>
)

data class WasteType(
    val code: String,
    val name: String,
    val examples: String,
    val recyclability: String
)

data class ProjectItem(
    val id: Int,
    val title: String,
    val category: String,
    val progress: Float, // 0.0 to 1.0
    val isMaterialReady: Boolean,
    val status: String, // "Belum Dimulai", "Berjalan", "Selesai"
    val color: Color,
    val impact: String = "Mengurangi 50g limbah",
    val steps: List<String> = listOf("Siapkan bahan", "Potong pola", "Rakit bagian", "Finishing"),
    val imageUrl: String = ""
)

data class InventoryItem(
    val id: Long,
    val name: String,
    val category: String,
    val dateAdded: String,
    val weight: String,
    val status: String
)

data class CommunityPost(
    val id: Int,
    val title: String,
    val creatorName: String,
    val category: String,
    val impact: String,
    val likes: Int,
    val color: Color,
    val imageUrl: String = ""
)

data class UserBadge(
    val id: String,
    val name: String,
    val requiredPoints: Int,
    val icon: ImageVector,
    val isClaimed: Boolean = false
)

enum class ActivityType {
    SCAN,
    PROJECT_START,
    PROJECT_COMPLETE,
    COMMUNITY_POST,
    COMMUNITY_LIKE,
    BADGE_UNLOCK
}

data class ActivityEvent(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: Long,
    val iconName: String = "", // For Firestore serialization
    val colorInt: Long = 0xFF2196F3 // For Firestore serialization
) {
    // Helper to get icon from type
    fun getIcon(): ImageVector = when (type) {
        ActivityType.SCAN -> Icons.Default.CameraAlt
        ActivityType.PROJECT_START -> Icons.Default.Build
        ActivityType.PROJECT_COMPLETE -> Icons.Default.CheckCircle
        ActivityType.COMMUNITY_POST -> Icons.Default.Share
        ActivityType.COMMUNITY_LIKE -> Icons.Default.Favorite
        ActivityType.BADGE_UNLOCK -> Icons.Default.EmojiEvents
    }
    
    // Helper to get color from type
    fun getColor(): Color = when (type) {
        ActivityType.SCAN -> Color(0xFF2196F3) // Blue
        ActivityType.PROJECT_START -> Color(0xFFFF9800) // Orange
        ActivityType.PROJECT_COMPLETE -> Color(0xFF4CAF50) // Green
        ActivityType.COMMUNITY_POST -> Color(0xFF9C27B0) // Purple
        ActivityType.COMMUNITY_LIKE -> Color(0xFFE91E63) // Pink
        ActivityType.BADGE_UNLOCK -> Color(0xFFFFD700) // Gold
    }
}

// --- Data Source ---

object UpcycleData {
    private val db = Firebase.firestore

    var currentUser: String by mutableStateOf("Kamu")
    var userPoints: Int by mutableStateOf(0)
    var userLevel: String by mutableStateOf("Eco Beginner")
    var currentUserId: String = "" 

    // Badges State
    val userBadges = mutableStateListOf<UserBadge>()
    
    // Activity History State
    val activityHistory = mutableStateListOf<ActivityEvent>()

    // Initialize Standard Badges
    private val allBadges = listOf(
        UserBadge("b1", "Pemula", 100, Icons.Default.Star),
        UserBadge("b2", "Rajin", 300, Icons.Default.Recycling),
        UserBadge("b3", "Kreatif", 600, Icons.Default.EmojiEvents),
        UserBadge("b4", "Master", 1000, Icons.Default.Eco),
        UserBadge("b5", "Influencer", 2000, Icons.Default.Person),
        UserBadge("b6", "Guru", 5000, Icons.Default.Lightbulb)
    )

    // Synchronize local state with Firestore Profile
    fun syncUserProfile(uid: String, onSuccess: () -> Unit = {}) {
        currentUserId = uid
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    currentUser = doc.getString("username") ?: "User"
                    userPoints = doc.getLong("points")?.toInt() ?: 0
                    userLevel = doc.getString("level") ?: "Eco Beginner"
                    
                    // Load claimed badges
                    val claimedIds = (doc.get("claimedBadges") as? List<String>) ?: emptyList()
                    
                    userBadges.clear()
                    allBadges.forEach { badge ->
                        userBadges.add(badge.copy(isClaimed = claimedIds.contains(badge.id)))
                    }

                    // Load data after login
                    loadInventory()
                    loadProjects()
                    loadIdeas() // Fetch Ideas
                    loadCommunityPosts() // Fetch Community Posts
                    loadActivityHistory() // Fetch Activity History
                    
                    // Auto-migrate existing data to add imageUrl if missing
                    migrateIdeasWithImages()
                    migratePostsWithImages()
                    migrateProjectsWithImages() // Auto-update projects with imageUrl
                    
                    onSuccess()
                }
            }
    }
    
    // Claim Badge Logic
    fun claimBadge(badgeId: String, onSuccess: () -> Unit = {}) {
        if (currentUserId.isEmpty()) return
        
        val badgeIndex = userBadges.indexOfFirst { it.id == badgeId }
        if (badgeIndex != -1) {
            val badge = userBadges[badgeIndex]
            if (userPoints >= badge.requiredPoints && !badge.isClaimed) {
                // Update Local
                userBadges[badgeIndex] = badge.copy(isClaimed = true)
                
                // Update Firestore
                val claimedIds = userBadges.filter { it.isClaimed }.map { it.id }
                db.collection("users").document(currentUserId)
                    .update("claimedBadges", claimedIds)
                    .addOnSuccessListener { onSuccess() }
            }
        }
    }
    
    // Update Points in Firestore
    fun updateUserPoints(newPoints: Int) {
        if (currentUserId.isEmpty()) return
        db.collection("users").document(currentUserId)
            .update("points", newPoints)
            .addOnSuccessListener {
                userPoints = newPoints
            }
    }

    // --- Inventory Management ---
    val inventory = mutableStateListOf<InventoryItem>()

    fun loadInventory() {
        if (currentUserId.isEmpty()) return
        
        db.collection("users").document(currentUserId).collection("inventory")
            .get()
            .addOnSuccessListener { result ->
                inventory.clear()
                for (document in result) {
                    val item = InventoryItem(
                        id = document.getLong("id") ?: 0L,
                        name = document.getString("name") ?: "",
                        category = document.getString("category") ?: "",
                        dateAdded = document.getString("dateAdded") ?: "",
                        weight = document.getString("weight") ?: "",
                        status = document.getString("status") ?: ""
                    )
                    inventory.add(item)
                }
            }
    }

    fun addInventoryItem(item: InventoryItem, onSuccess: () -> Unit = {}) {
        if (currentUserId.isEmpty()) return

        val itemMap = hashMapOf(
            "id" to item.id,
            "name" to item.name,
            "category" to item.category,
            "dateAdded" to item.dateAdded,
            "weight" to item.weight,
            "status" to item.status
        )

        db.collection("users").document(currentUserId).collection("inventory")
            .add(itemMap)
            .addOnSuccessListener {
                inventory.add(item) 
                onSuccess()
            }
    }

    // --- Project Management ---
    val projects = mutableStateListOf<ProjectItem>()

    fun loadProjects() {
        if (currentUserId.isEmpty()) return

        db.collection("users").document(currentUserId).collection("projects")
            .get()
            .addOnSuccessListener { result ->
                projects.clear()
                for (document in result) {
                    val colorInt = document.getLong("color")?.toInt() ?: 0xFF8CA993.toInt()
                    val item = ProjectItem(
                        id = document.getLong("id")?.toInt() ?: 0,
                        title = document.getString("title") ?: "",
                        category = document.getString("category") ?: "",
                        progress = document.getDouble("progress")?.toFloat() ?: 0f,
                        isMaterialReady = document.getBoolean("isMaterialReady") ?: true,
                        status = document.getString("status") ?: "Belum Dimulai",
                        color = Color(colorInt),
                        impact = document.getString("impact") ?: "",
                        steps = (document.get("steps") as? List<String>) ?: emptyList(),
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    projects.add(item)
                }
            }
    }

    fun addProject(item: ProjectItem) {
        if (currentUserId.isEmpty()) return

        // Convert Color to ARGB Int (simple hashcode or toArgb if available, using value.toLong for compose Color)
        val colorInt = item.color.value.toLong().toInt() 

        val itemMap = hashMapOf(
            "id" to item.id,
            "title" to item.title,
            "category" to item.category,
            "progress" to item.progress,
            "isMaterialReady" to item.isMaterialReady,
            "status" to item.status,
            "color" to colorInt,
            "impact" to item.impact,
            "steps" to item.steps,
            "imageUrl" to item.imageUrl
        )

        db.collection("users").document(currentUserId).collection("projects")
            .document(item.id.toString())
            .set(itemMap)
            .addOnSuccessListener {
                if (projects.none { it.id == item.id }) {
                    projects.add(0, item)
                }
            }
    }

    fun updateProjectProgress(item: ProjectItem) {
        if (currentUserId.isEmpty()) return

        val updates = mapOf(
            "status" to item.status,
            "progress" to item.progress
        )

        db.collection("users").document(currentUserId).collection("projects")
            .document(item.id.toString())
            .update(updates)
    }
    
    // --- Community Posts Management ---
    
    val posts = mutableStateListOf<CommunityPost>()

    // Load All Community Posts
    fun loadCommunityPosts() {
        db.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val colorInt = document.getLong("color")?.toInt() ?: 0xFF8CA993.toInt()
                    val post = CommunityPost(
                        id = document.getLong("id")?.toInt() ?: 0,
                        title = document.getString("title") ?: "",
                        creatorName = document.getString("creatorName") ?: "Anonim",
                        category = document.getString("category") ?: "",
                        impact = document.getString("impact") ?: "",
                        likes = document.getLong("likes")?.toInt() ?: 0,
                        color = Color(colorInt),
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    posts.add(post)
                }
            }
    }

    // Add Post to Firestore
    fun addCommunityPost(post: CommunityPost, onSuccess: () -> Unit = {}) {
        val colorInt = post.color.value.toLong().toInt()
        
        val postMap = hashMapOf(
            "id" to post.id,
            "title" to post.title,
            "creatorName" to post.creatorName,
            "category" to post.category,
            "impact" to post.impact,
            "likes" to post.likes,
            "color" to colorInt,
            "imageUrl" to post.imageUrl,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("posts")
            .document(post.id.toString())
            .set(postMap)
            .addOnSuccessListener {
                if (posts.none { it.id == post.id }) {
                    posts.add(0, post)
                }
                onSuccess()
            }
    }

    fun addProjectFromCommunity(post: CommunityPost) {
        val newProject = ProjectItem(
            id = (System.currentTimeMillis() / 1000).toInt(),
            title = post.title,
            category = post.category,
            progress = 0.0f,
            isMaterialReady = true,
            status = "Belum Dimulai",
            color = post.color,
            impact = "Menyelamatkan ${post.impact}",
            steps = listOf(
                "Kumpulkan bahan ${post.category} yang diperlukan.",
                "Bersihkan dan siapkan bahan.",
                "Ikuti panduan kreasi (lihat detail komunitas).",
                "Finishing dan hias sesuai selera."
            ),
            imageUrl = post.imageUrl // Sync image from community post
        )
        addProject(newProject)
    }

    fun addProjectFromIdea(idea: UpcycleIdea) {
        val newProject = ProjectItem(
            id = (System.currentTimeMillis() / 1000).toInt(),
            title = idea.title,
            category = idea.category,
            progress = 0.0f,
            isMaterialReady = true,
            status = "Belum Dimulai",
            color = idea.color,
            impact = "Proyek dari Ide Pilihan",
            steps = idea.steps.ifEmpty { listOf("Siapkan alat dan bahan", "Ikuti instruksi pengerjaan", "Finishing") },
            imageUrl = idea.imageUrl // Sync image from idea
        )
        addProject(newProject)
    }

    fun addPostFromProject(project: ProjectItem) {
        val newPost = CommunityPost(
            id = (System.currentTimeMillis() / 1000).toInt(),
            title = project.title,
            creatorName = currentUser,
            category = project.category,
            impact = project.impact.removePrefix("Menyelamatkan "),
            likes = 0,
            color = project.color
        )
        addCommunityPost(newPost)
    }

    // --- Seeding Data ---
    fun seedInitialData() {
        val dummyPosts = listOf(
            CommunityPost(1, "Dompet Sachet Kopi", "Santi_Recycle", "Plastik", "5 Sachet", 120, Color(0xFF2196F3), "https://images.unsplash.com/photo-1624823183493-ed5832f48f18?w=800&q=80"),
            CommunityPost(2, "Lampu Tidur Stik Es", "Budi_Craft", "Kayu", "50 Stik Es", 85, Color(0xFFFF9800), "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&q=80"),
            CommunityPost(3, "Rak Buku Kardus", "Rina_Lestari", "Kertas", "2kg Kardus", 200, Color(0xFFFFC107), "https://images.unsplash.com/photo-1594620302200-9a762244a156?w=800&q=80"),
            CommunityPost(4, "Vas Bunga Botol", "Eco_Warrior", "Plastik", "1 Botol", 45, Color(0xFF4CAF50), "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800&q=80"),
            CommunityPost(5, "Tas Belanja Kain", "Mama_Jahit", "Tekstil", "1 Baju Bekas", 150, Color(0xFF9C27B0), "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800&q=80"),
            CommunityPost(6, "Pot Gantung Kaleng", "Green_Thumb", "Logam", "2 Kaleng", 90, Color(0xFF607D8B), "https://images.unsplash.com/photo-1466692476868-aef1dfb1e735?w=800&q=80")
        )

        db.collection("posts").get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                dummyPosts.forEach { post ->
                    addCommunityPost(post)
                }
            }
        }
    }

    // --- Static Data ---

    // --- Ideas Management (Master Data) ---

    val ideas = mutableStateListOf<UpcycleIdea>()

    fun loadIdeas() {
        db.collection("ideas")
            .get()
            .addOnSuccessListener { result ->
                ideas.clear()
                for (document in result) {
                    val colorInt = document.getLong("color")?.toInt() ?: 0xFF8CA993.toInt()
                    val idea = UpcycleIdea(
                        id = document.getLong("id")?.toInt() ?: 0,
                        title = document.getString("title") ?: "",
                        difficulty = document.getString("difficulty") ?: "",
                        timeRequired = document.getString("timeRequired") ?: "",
                        description = document.getString("description") ?: "",
                        tools = (document.get("tools") as? List<String>) ?: emptyList(),
                        materials = (document.get("materials") as? List<String>) ?: emptyList(),
                        steps = (document.get("steps") as? List<String>) ?: emptyList(),
                        category = document.getString("category") ?: "",
                        color = Color(colorInt),
                        imageUrl = document.getString("imageUrl") ?: ""
                    )
                    ideas.add(idea)
                }
            }
    }

    fun addIdea(idea: UpcycleIdea) {
        val colorInt = idea.color.value.toLong().toInt()
        
        val ideaMap = hashMapOf(
            "id" to idea.id,
            "title" to idea.title,
            "difficulty" to idea.difficulty,
            "timeRequired" to idea.timeRequired,
            "description" to idea.description,
            "tools" to idea.tools,
            "materials" to idea.materials,
            "steps" to idea.steps,
            "category" to idea.category,
            "color" to colorInt,
            "imageUrl" to idea.imageUrl
        )

        db.collection("ideas").document(idea.id.toString()).set(ideaMap)
            .addOnSuccessListener {
                if (ideas.none { it.id == idea.id }) {
                    ideas.add(idea)
                }
            }
    }

    fun seedInitialIdeas() {
        val dummyIdeas = listOf(
            UpcycleIdea(1, "Pot Bunga Botol Gantung", "Mudah", "30 Menit", "Ubah botol plastik bekas menjadi pot gantung cantik.", listOf("Gunting"), listOf("Botol 1.5L"), listOf("Bersihkan", "Potong", "Tanam"), "Plastik", Color(0xFF2196F3), "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800&q=80"),
            UpcycleIdea(2, "Dompet Koin Kemasan", "Sedang", "1 Jam", "Jahit kemasan sachet menjadi dompet.", listOf("Jarum"), listOf("Sachet"), listOf("Cuci", "Jahit"), "Plastik", Color(0xFF2196F3), "https://images.unsplash.com/photo-1591561954557-26941169b49e?w=800&q=80"),
            UpcycleIdea(3, "Lampu Hias Toples", "Mudah", "45 Menit", "Ciptakan suasana hangat dengan toples.", listOf("Lem"), listOf("Toples"), listOf("Bersihkan", "Isi Lampu"), "Kaca", Color(0xFF00BCD4), "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=800&q=80"),
            UpcycleIdea(4, "Organizer Meja", "Sedang", "1.5 Jam", "Rapikan meja dengan kardus.", listOf("Cutter"), listOf("Kardus"), listOf("Ukur", "Rakit"), "Kertas", Color(0xFFFFC107), "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=800&q=80")
        )

        db.collection("ideas").get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                dummyIdeas.forEach { addIdea(it) }
            }
        }
    }

    val categories = listOf(
        WasteCategory("Plastik", "Plastik", "Botol, kemasan, plastik.", Icons.Default.LocalDrink, Color(0xFF2196F3), listOf(WasteType("PET", "Polyethylene", "Botol mineral", "Tinggi")), listOf("Remas botol")),
        WasteCategory("Kertas", "Kertas", "Koran, kardus, majalah.", Icons.Default.Feed, Color(0xFFFFC107), listOf(WasteType("Karton", "Cardboard", "Kardus paket", "Tinggi")), listOf("Pipihkan kardus")),
        WasteCategory("Kaca", "Kaca", "Botol selai, toples.", Icons.Default.LocalBar, Color(0xFF00BCD4), listOf(WasteType("Bening", "Glass", "Toples selai", "Tinggi")), listOf("Cuci bersih")),
        WasteCategory("Logam", "Logam", "Kaleng, besi, alu.", Icons.Default.PrecisionManufacturing, Color(0xFF607D8B), listOf(WasteType("Alu", "Aluminium", "Kaleng soda", "Tinggi")), listOf("Cuci bersih")),
        WasteCategory("Tekstil", "Tekstil", "Pakaian bekas, kain perca.", Icons.Default.Checkroom, Color(0xFF9C27B0), listOf(WasteType("Katun", "Kaos", "Kaos bekas", "Tinggi")), listOf("Cuci bersih"))
    )
    
    // --- Activity History Management ---
    
    fun addActivityEvent(event: ActivityEvent) {
        if (currentUserId.isEmpty()) return
        
        val eventMap = hashMapOf(
            "id" to event.id,
            "type" to event.type.name,
            "title" to event.title,
            "description" to event.description,
            "timestamp" to event.timestamp
        )
        
        db.collection("users").document(currentUserId).collection("activities")
            .document(event.id)
            .set(eventMap)
            .addOnSuccessListener {
                if (activityHistory.none { it.id == event.id }) {
                    activityHistory.add(0, event) // Add to beginning
                }
            }
    }
    
    fun loadActivityHistory() {
        if (currentUserId.isEmpty()) return
        
        db.collection("users").document(currentUserId).collection("activities")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100) // Limit to last 100 events
            .get()
            .addOnSuccessListener { result ->
                activityHistory.clear()
                for (document in result) {
                    val typeString = document.getString("type") ?: "SCAN"
                    val type = try {
                        ActivityType.valueOf(typeString)
                    } catch (e: Exception) {
                        ActivityType.SCAN
                    }
                    
                    val event = ActivityEvent(
                        id = document.getString("id") ?: "",
                        type = type,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        timestamp = document.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                    activityHistory.add(event)
                }
            }
    }
    
    // --- Migration Functions ---
    
    // Auto-update ideas with imageUrl if missing
    fun migrateIdeasWithImages() {
        val imageUrls = mapOf(
            1 to "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800&q=80",
            2 to "https://images.unsplash.com/photo-1591561954557-26941169b49e?w=800&q=80",
            3 to "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=800&q=80",
            4 to "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=800&q=80"
        )
        
        db.collection("ideas").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.getLong("id")?.toInt() ?: 0
                    val currentImageUrl = document.getString("imageUrl") ?: ""
                    
                    // Only update if imageUrl is empty or missing
                    if (currentImageUrl.isEmpty() && imageUrls.containsKey(id)) {
                        db.collection("ideas").document(document.id)
                            .update("imageUrl", imageUrls[id])
                    }
                }
            }
    }
    
    // Auto-update community posts with imageUrl if missing
    fun migratePostsWithImages() {
        val imageUrls = mapOf(
            1 to "https://images.unsplash.com/photo-1624823183493-ed5832f48f18?w=800&q=80",
            2 to "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&q=80",
            3 to "https://images.unsplash.com/photo-1594620302200-9a762244a156?w=800&q=80",
            4 to "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800&q=80",
            5 to "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800&q=80",
            6 to "https://images.unsplash.com/photo-1466692476868-aef1dfb1e735?w=800&q=80"
        )
        
        db.collection("posts").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.getLong("id")?.toInt() ?: 0
                    val currentImageUrl = document.getString("imageUrl") ?: ""
                    
                    // Only update if imageUrl is empty or missing
                    if (currentImageUrl.isEmpty() && imageUrls.containsKey(id)) {
                        db.collection("posts").document(document.id)
                            .update("imageUrl", imageUrls[id])
                    }
                }
            }
    }
    
    // Auto-update projects with imageUrl if missing
    fun migrateProjectsWithImages() {
        if (currentUserId.isEmpty()) return
        
        // Map project titles to their corresponding imageUrl
        val titleToImageUrl = mapOf(
            "Pot Bunga Botol Gantung" to "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800&q=80",
            "Dompet Koin Kemasan" to "https://images.unsplash.com/photo-1591561954557-26941169b49e?w=800&q=80",
            "Lampu Hias Toples" to "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=800&q=80",
            "Organizer Meja" to "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=800&q=80",
            // Community posts
            "Dompet Sachet Kopi" to "https://images.unsplash.com/photo-1624823183493-ed5832f48f18?w=800&q=80",
            "Lampu Tidur Stik Es" to "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&q=80",
            "Rak Buku Kardus" to "https://images.unsplash.com/photo-1594620302200-9a762244a156?w=800&q=80",
            "Vas Bunga Botol" to "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800&q=80",
            "Tas Belanja Kain" to "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800&q=80",
            "Pot Gantung Kaleng" to "https://images.unsplash.com/photo-1466692476868-aef1dfb1e735?w=800&q=80"
        )
        
        db.collection("users").document(currentUserId).collection("projects").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val currentImageUrl = document.getString("imageUrl") ?: ""
                    
                    // Only update if imageUrl is empty or missing
                    if (currentImageUrl.isEmpty() && titleToImageUrl.containsKey(title)) {
                        db.collection("users").document(currentUserId).collection("projects")
                            .document(document.id)
                            .update("imageUrl", titleToImageUrl[title])
                            .addOnSuccessListener {
                                // Reload projects after update
                                loadProjects()
                            }
                    }
                }
            }
    }
}