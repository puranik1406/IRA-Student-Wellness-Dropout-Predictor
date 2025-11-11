package com.example.ira.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ira.IRAApplication
import com.example.ira.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class StudentNotification(
    val id: Long,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    navController: NavController,
    studentId: Long,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as IRAApplication
    val repository = application.repository
    val sessionManager = application.sessionManager
    
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Get student data from database
    val student by repository.getStudentById(studentId).collectAsState(initial = null)

    // Get actual mood and journal data
    val recentMoodsRaw by repository.getRecentMoods(studentId, 5)
        .collectAsState(initial = emptyList())
    val recentJournalsRaw by repository.getRecentJournals(studentId, 3)
        .collectAsState(initial = emptyList())

    // Transform recent mood and journal data to Triple format for display
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val recentMoods = recentMoodsRaw.map { mood ->
        Triple(
            mood.moodScore,
            mood.notes.ifBlank { "No notes" },
            dateFormat.format(Date(mood.createdAt))
        )
    }

    val recentJournals = recentJournalsRaw.map { journal ->
        Triple(
            journal.title,
            if (journal.content.length > 80) journal.content.substring(
                0,
                80
            ) + "..." else journal.content,
            dateFormat.format(Date(journal.createdAt))
        )
    }

    // State for notifications
    var showNotifications by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(listOf<StudentNotification>()) }
    var unreadCount by remember { mutableIntStateOf(0) }

    // Mock notifications
    LaunchedEffect(Unit) {
        notifications = listOf(
            StudentNotification(
                1,
                "Counseling Session Scheduled",
                "A counselor has scheduled a session with you. You will be contacted soon.",
                false,
                "2 hours ago"
            )
        )
        unreadCount = notifications.count { !it.isRead }
    }

    // Get student data or use defaults
    val studentName = student?.name ?: "Student"
    val rollNumber = student?.rollNumber ?: "N/A"
    val department = student?.department ?: "N/A"
    val semester = student?.semester ?: 0
    val averageAttendance = 87.5f // TODO: Get from repository
    val feePending = student?.feePending ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "IRA Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "Student Portal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White
                ),
                actions = {
                    // Notification Button
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = DangerRed) {
                                    Text(
                                        if (unreadCount > 99) "99+" else unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showNotifications = !showNotifications }) {
                            Text("🔔", fontSize = 24.sp)
                        }
                    }

                    IconButton(onClick = {
                        scope.launch {
                            sessionManager.clearSession()
                            onLogout()
                        }
                    }) {
                        Text("🚪", fontSize = 24.sp)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightGray)
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // Welcome Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Lavender, SkyBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👤",
                                fontSize = 40.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Welcome back, $studentName! 👋",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Navy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$rollNumber | $department | Semester $semester",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Main Stats Row - Attendance & Fee Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Attendance Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = SuccessGreen
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📅",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "${averageAttendance}%",
                                fontSize = 30.sp,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                "Average Attendance",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Fee Status Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (feePending) WarningYellow else MintGreen
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💰",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (feePending) "Pending" else "Paid",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                "Fee Status",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Wellness Insights
                WellnessInsightsCard(
                    onScheduleMeeting = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Meeting request sent. You will receive a call from counselor.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mood Trends
                MoodTrendsCard(
                    recentMoods = recentMoods,
                    onClick = { navController.navigate("mood/$studentId") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Activity & Sleep
                ActivitySleepCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Recent Journals
                RecentJournalsCard(
                    recentJournals = recentJournals,
                    onClick = { navController.navigate("journal/$studentId") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Actions
                QuickActionsSection(
                    onMoodClick = { navController.navigate("mood/$studentId") },
                    onJournalClick = { navController.navigate("journal/$studentId") }
                )

                Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB
            }

            // Floating Action Button - AI Chatbot
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("chatbot/$studentId")
                    },
                    modifier = Modifier
                        .padding(24.dp)
                        .size(70.dp),
                    containerColor = Navy,
                    contentColor = Color.White
                ) {
                    Text("🧸", fontSize = 36.sp)
                }
            }

            // Notification Panel
            if (showNotifications) {
                StudentNotificationPanel(
                    notifications = notifications,
                    onDismiss = { showNotifications = false },
                    onNotificationClick = { notification ->
                        // Mark as read
                        showNotifications = false
                    }
                )
            }
        }
    }
}

@Composable
fun StudentNotificationPanel(
    notifications: List<StudentNotification>,
    onDismiss: () -> Unit,
    onNotificationClick: (StudentNotification) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .padding(top = 60.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔔", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Text("✖", fontSize = 18.sp, color = Color.White)
                    }
                }
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        notifications.forEach {
                            StudentNotificationItem(it) { onNotificationClick(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentNotificationItem(notification: StudentNotification, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) CardBackground else InfoBlue.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Navy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    notification.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            if (!notification.isRead) {
                Badge(containerColor = DangerRed) {
                    Text("New", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun WellnessInsightsCard(onScheduleMeeting: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AI Wellness Insights",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Navy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rotating wellness tips
            val tips = listOf(
                "🧘‍♀️" to "Take a 5-minute mindfulness break today",
                "💧" to "Stay hydrated! Drink at least 8 glasses of water",
                "🌙" to "Aim for 7-8 hours of quality sleep tonight",
                "🚶" to "Take a short walk to refresh your mind",
                "📚" to "Break your study sessions into 25-minute intervals"
            )

            var currentTipIndex by remember { mutableStateOf(0) }

            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(5000)
                    currentTipIndex = (currentTipIndex + 1) % tips.size
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Lavender.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        tips[currentTipIndex].first,
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        tips[currentTipIndex].second,
                        style = MaterialTheme.typography.titleMedium,
                        color = Navy,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onScheduleMeeting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Text("📅", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule Counselor Meeting",
                color = Color.White)
            }
        }
    }
}

@Composable
fun MoodTrendsCard(
    recentMoods: List<Triple<Int, String, String>>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("😊", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Recent Mood Check-ins",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Navy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (recentMoods.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("😊", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No mood entries yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Start tracking your mood to see trends",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                recentMoods.forEach { (score, note, date) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = when {
                                    score >= 8 -> SuccessGreen
                                    score >= 6 -> InfoBlue
                                    score >= 4 -> WarningYellow
                                    else -> DangerRed
                                }
                            ) {
                                Text(
                                    "$score/10",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Text(
                            date,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    if (recentMoods.last() != recentMoods.find { it.third == date }) {
                        HorizontalDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("➕", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Mood")
            }
        }
    }
}

@Composable
fun ActivitySleepCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📊", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Activity & Sleep (Last 7 Days)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Navy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mock activity data
            val activities = listOf(
                Triple("2024-01-15", "8,500 steps | 7h sleep | 30min exercise", true),
                Triple("2024-01-14", "7,200 steps | 6h sleep | 20min exercise", false),
                Triple("2024-01-13", "9,100 steps | 8h sleep | 45min exercise", true),
                Triple("2024-01-12", "6,800 steps | 7h sleep | 30min exercise", true),
                Triple("2024-01-11", "5,400 steps | 5h sleep | 15min exercise", false)
            )

            activities.forEach { (date, data, isGood) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGood)
                            SuccessGreen.copy(alpha = 0.1f)
                        else
                            WarningYellow.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            date,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            data,
                            style = MaterialTheme.typography.bodySmall,
                            color = Navy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = InfoBlue.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ℹ️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Data synced from fitness app (mock)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Navy
                    )
                }
            }
        }
    }
}

@Composable
fun RecentJournalsCard(
    recentJournals: List<Triple<String, String, String>>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📔", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Recent Journal Entries",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Navy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (recentJournals.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📖", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No journal entries yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                recentJournals.forEach { (title, content, date) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Lavender.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Navy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                date,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✏️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Write Journal")
            }
        }
    }
}

@Composable
fun QuickActionsSection(onMoodClick: () -> Unit, onJournalClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Navy,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                emoji = "😊",
                label = "Log Mood",
                onClick = onMoodClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                emoji = "📝",
                label = "Journal",
                onClick = onJournalClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                emoji = "📅",
                label = "Attendance",
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                emoji = "❤️",
                label = "Wellness",
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Navy,
                textAlign = TextAlign.Center
            )
        }
    }
}
