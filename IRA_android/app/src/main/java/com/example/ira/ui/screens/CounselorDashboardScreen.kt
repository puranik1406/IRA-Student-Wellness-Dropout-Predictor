package com.example.ira.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ira.IRAApplication
import com.example.ira.ui.theme.*
import kotlinx.coroutines.launch

data class StudentRiskInfo(
    val id: Long,
    val name: String,
    val rollNumber: String,
    val department: String,
    val semester: Int,
    val email: String,
    val cgpa: Float,
    val riskLevel: String,
    val riskFactors: Map<String, String>,
    val moodTrend: List<Float>,
    val averageSleepHours: Float,
    val attendancePercentage: Float,
    val averageSteps: Int,
    val exerciseMinutes: Int
)

data class Meeting(
    val id: Long,
    val studentName: String,
    val rollNumber: String,
    val scheduledAt: String,
    val status: String
)

data class Notification(
    val id: Long,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val referenceId: Long?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounselorDashboardScreen(
    navController: NavController,
    counselorId: Long,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as IRAApplication
    val sessionManager = application.sessionManager
    
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    // State for notifications
    var showNotifications by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    var unreadCount by remember { mutableIntStateOf(0) }

    // Mock notifications
    LaunchedEffect(Unit) {
        notifications = listOf(
            Notification(
                1,
                "New Meeting Request",
                "Rahul Sharma (21BCS101) has requested a counseling session.",
                false,
                1
            ),
            Notification(
                2,
                "New Meeting Request",
                "Sneha Kumar (21BCS104) has requested a counseling session.",
                false,
                4
            )
        )
        unreadCount = notifications.count { !it.isRead }
    }

    // Mock student data - sorted by risk level
    val students = remember {
        listOf(
            // High Risk
            StudentRiskInfo(
                1,
                "Rahul Sharma",
                "21BCS101",
                "Computer Science",
                6,
                "rahul@university.edu",
                6.2f,
                "high",
                mapOf(
                    "Attendance" to "Critical - Below 65%",
                    "Mood" to "Concerning - Declining trend",
                    "Academic" to "CGPA dropped 0.8 points"
                ),
                listOf(7f, 6f, 5f, 4f, 4f, 3f, 4f),
                5.5f,
                62f,
                3500,
                15
            ),
            StudentRiskInfo(
                4,
                "Sneha Kumar",
                "21BCS104",
                "Computer Science",
                6,
                "sneha@university.edu",
                6.8f,
                "high",
                mapOf(
                    "Attendance" to "Critical - Below 70%",
                    "Engagement" to "Low class participation"
                ),
                listOf(6f, 5f, 6f, 5f, 4f, 4f, 3f),
                6.0f,
                68f,
                4200,
                20
            ),
            // Moderate Risk
            StudentRiskInfo(
                2,
                "Priya Patel",
                "21BCS102",
                "Computer Science",
                6,
                "priya@university.edu",
                7.5f,
                "moderate",
                mapOf(
                    "Sleep" to "Concerning - Average 5h per night",
                    "Mood" to "Fluctuating mood scores"
                ),
                listOf(6f, 7f, 5f, 6f, 5f, 6f, 6f),
                5.0f,
                78f,
                6500,
                30
            ),
            StudentRiskInfo(
                5,
                "Arjun Reddy",
                "21BCS105",
                "Computer Science",
                6,
                "arjun@university.edu",
                7.8f,
                "moderate",
                mapOf("Mood" to "Recent decline in mood scores"),
                listOf(8f, 7f, 6f, 6f, 5f, 6f, 6f),
                6.5f,
                82f,
                7200,
                35
            ),
            // Low Risk
            StudentRiskInfo(
                3,
                "Ankit Verma",
                "21BCS103",
                "Computer Science",
                6,
                "ankit@university.edu",
                8.8f,
                "low",
                mapOf("General" to "Good overall performance"),
                listOf(8f, 8f, 9f, 8f, 9f, 8f, 8f),
                7.5f,
                92f,
                9500,
                45
            )
        )
    }

    val meetings = remember {
        listOf(
            Meeting(1, "Rahul Sharma", "21BCS101", "2024-01-16 10:00", "scheduled"),
            Meeting(2, "Priya Patel", "21BCS102", "2024-01-16 14:30", "scheduled"),
            Meeting(3, "Sneha Kumar", "21BCS104", "2024-01-17 11:00", "scheduled")
        )
    }

    var selectedFilter by remember { mutableStateOf("all") }
    var expandedStudentId by remember { mutableStateOf<Long?>(null) }

    val filteredStudents = when (selectedFilter) {
        "high" -> students.filter { it.riskLevel == "high" }
        "moderate" -> students.filter { it.riskLevel == "moderate" }
        "low" -> students.filter { it.riskLevel == "low" }
        else -> students
    }

    val riskCounts = mapOf(
        "high" to students.count { it.riskLevel == "high" },
        "moderate" to students.count { it.riskLevel == "moderate" },
        "low" to students.count { it.riskLevel == "low" }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Counselor Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Monitor student wellbeing",
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
                    BadgedBox(badge = {
                        if (unreadCount > 0) Badge(containerColor = DangerRed) {
                            Text(
                                if (unreadCount > 99) "99+" else "$unreadCount",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }) {
                        IconButton(onClick = { showNotifications = !showNotifications }) {
                            Text(
                                "🔔",
                                fontSize = 24.sp
                            )
                        }
                    }
                    IconButton(onClick = {
                        scope.launch {
                            sessionManager.clearSession()
                            onLogout()
                        }
                    }) { Text("🚪", fontSize = 24.sp) }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLandscape) {
                // Landscape: Side-by-side layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightGray)
                        .padding(paddingValues)
                ) {
                    // Main content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(scrollState)
                    ) {
                        MainContent(
                            students = students,
                            selectedFilter = selectedFilter,
                            onFilterChange = { selectedFilter = it },
                            riskCounts = riskCounts,
                            filteredStudents = filteredStudents,
                            expandedStudentId = expandedStudentId,
                            onToggleExpand = { expandedStudentId = it },
                            onScheduleMeeting = { studentName ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Meeting scheduled with $studentName. Student has been notified.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }

                    // Sidebar
                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        UpcomingSessionsContent(meetings)
                    }
                }
            } else {
                // Portrait: Stacked layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightGray)
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    MainContent(
                        students = students,
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it },
                        riskCounts = riskCounts,
                        filteredStudents = filteredStudents,
                        expandedStudentId = expandedStudentId,
                        onToggleExpand = { expandedStudentId = it },
                        onScheduleMeeting = { studentName ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Meeting scheduled with $studentName. Student has been notified.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )

                    // Upcoming Sessions below in portrait
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        UpcomingSessionsContent(meetings)
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Notification Panel
            if (showNotifications) {
                NotificationPanel(notifications, { showNotifications = false }, { notification ->
                    notification.referenceId?.let {
                        expandedStudentId = it; showNotifications = false
                    }
                })
            }
        }
    }
}

@Composable
fun MainContent(
    students: List<StudentRiskInfo>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    riskCounts: Map<String, Int>,
    filteredStudents: List<StudentRiskInfo>,
    expandedStudentId: Long?,
    onToggleExpand: (Long?) -> Unit,
    onScheduleMeeting: (String) -> Unit
) {
    // Risk Counters
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RiskCounterCard(
            "⚠️",
            riskCounts["high"] ?: 0,
            "High Risk",
            DangerRed,
            Modifier.weight(1f),
            { onFilterChange(if (selectedFilter == "high") "all" else "high") },
            selectedFilter == "high"
        )
        RiskCounterCard(
            "⚠️",
            riskCounts["moderate"] ?: 0,
            "Moderate Risk",
            WarningYellow,
            Modifier.weight(1f),
            { onFilterChange(if (selectedFilter == "moderate") "all" else "moderate") },
            selectedFilter == "moderate"
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RiskCounterCard(
            "✅",
            riskCounts["low"] ?: 0,
            "Low Risk",
            SuccessGreen,
            Modifier.weight(1f),
            { onFilterChange(if (selectedFilter == "low") "all" else "low") },
            selectedFilter == "low"
        )
        RiskCounterCard(
            "👤",
            students.size,
            "Total Students",
            Navy,
            Modifier.weight(1f),
            { onFilterChange("all") },
            selectedFilter == "all"
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Student List
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📋", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Students by Risk Level",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Navy
                    )
                    Text(
                        "Click on a student to view detailed wellness data",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            filteredStudents.forEach { student ->
                StudentRiskCard(
                    student,
                    expandedStudentId == student.id,
                    {
                        onToggleExpand(
                            if (expandedStudentId == student.id) null else student.id
                        )
                    },
                    { onScheduleMeeting(student.name) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun UpcomingSessionsContent(meetings: List<Meeting>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📅", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Upcoming Sessions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Navy
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (meetings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📆", fontSize = 64.sp)
                Text(
                    "No upcoming sessions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            meetings.forEach {
                MeetingCard(it)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun NotificationPanel(
    notifications: List<Notification>,
    onDismiss: () -> Unit,
    onNotificationClick: (Notification) -> Unit
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
                        Text(
                            "✖",
                            fontSize = 18.sp,
                            color = Color.White
                        )
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
                        notifications.forEach { NotificationItem(it, { onNotificationClick(it) }) }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) CardBackground else InfoBlue.copy(
                alpha = 0.1f
            )
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
            }
            if (!notification.isRead) Badge(containerColor = DangerRed) {
                Text(
                    "New",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun RiskCounterCard(
    emoji: String,
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Navy) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StudentRiskCard(
    student: StudentRiskInfo,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onScheduleMeeting: () -> Unit
) {
    val borderColor = when (student.riskLevel) {
        "high" -> DangerRed
        "moderate" -> WarningYellow
        else -> SuccessGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(4.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Lavender, MintGreen))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        student.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Navy
                    )
                    Text(
                        "${student.rollNumber} | ${student.department} | Sem ${student.semester}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Badge(
                        containerColor = when (student.riskLevel) {
                            "high" -> DangerRed; "moderate" -> WarningYellow; else -> SuccessGreen
                        }
                    ) {
                        Text(
                            student.riskLevel.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge(containerColor = Navy) {
                        Text(
                            "CGPA: ${student.cgpa}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onToggleExpand, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (isExpanded) "▲ Hide Details" else "▼ View Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = Navy
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Student Info & Risk Factors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📧", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Student Info",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Navy
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Email: ${student.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Text(
                                "Department: ${student.department}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Text(
                                "Semester: ${student.semester}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            Text(
                                "CGPA: ${student.cgpa}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            if (student.riskLevel == "high") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onScheduleMeeting,
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("📅", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Schedule Meeting", fontSize = 12.sp)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Risk Factors",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DangerRed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            student.riskFactors.forEach { (key, value) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            value.contains("Critical") -> DangerRed.copy(alpha = 0.1f)
                                            value.contains("Concerning") -> WarningYellow.copy(alpha = 0.1f)
                                            else -> InfoBlue.copy(alpha = 0.1f)
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            key,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Navy
                                        )
                                        Text(
                                            value,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weekly Wellness Trends
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Weekly Wellness Trends",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = InfoBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Three cards: Mood, Sleep, Activity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mood
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = LightGray)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("😊", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Mood",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Navy
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    student.moodTrend.joinToString(" → ") { "%.0f".format(it) },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val avgMood = student.moodTrend.average()
                                Text(
                                    "Avg: ${"%.1f".format(avgMood)}/10",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when {
                                        avgMood >= 7 -> SuccessGreen; avgMood >= 5 -> WarningYellow; else -> DangerRed
                                    }
                                )
                            }
                        }
                        // Sleep
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = LightGray)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💤", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Sleep",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Navy
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "${student.averageSleepHours}h",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Navy
                                )
                                Text(
                                    "Avg Sleep",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = (student.averageSleepHours / 10f).coerceIn(0f, 1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = when {
                                        student.averageSleepHours >= 7 -> SuccessGreen; student.averageSleepHours >= 6 -> WarningYellow; else -> DangerRed
                                    },
                                    trackColor = LightGray
                                )
                            }
                        }
                        // Activity
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = LightGray)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏃", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Activity",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Navy
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "${student.averageSteps}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Navy
                                )
                                Text(
                                    "steps/day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${student.exerciseMinutes}min exercise",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Attendance
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📅", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Attendance",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Navy
                                    )
                                    Text(
                                        "${student.attendancePercentage}%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Navy
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                LinearProgressIndicator(
                                    progress = student.attendancePercentage / 100f,
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = when {
                                        student.attendancePercentage >= 85 -> SuccessGreen; student.attendancePercentage >= 75 -> WarningYellow; else -> DangerRed
                                    },
                                    trackColor = LightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    when {
                                        student.attendancePercentage >= 85 -> "Good"; student.attendancePercentage >= 75 -> "Fair"; else -> "Critical"
                                    },
                                    style = MaterialTheme.typography.labelSmall, color = when {
                                        student.attendancePercentage >= 85 -> SuccessGreen; student.attendancePercentage >= 75 -> WarningYellow; else -> DangerRed
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingCard(meeting: Meeting) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Lavender.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        meeting.studentName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Navy
                    )
                    Text(
                        meeting.rollNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Badge(containerColor = Navy) {
                    Text(
                        meeting.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🕐", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    meeting.scheduledAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}
