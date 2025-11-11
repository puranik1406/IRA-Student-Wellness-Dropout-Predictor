package com.example.ira.data.repository

import com.example.ira.data.local.dao.*
import com.example.ira.data.local.entities.*
import kotlinx.coroutines.flow.Flow

class IRARepository(
    private val studentDao: StudentDao,
    private val counselorDao: CounselorDao,
    private val moodDao: MoodDao,
    private val journalDao: JournalDao,
    private val activityDao: ActivityDao,
    private val attendanceDao: AttendanceDao,
    private val meetingDao: MeetingDao,
    private val notificationDao: NotificationDao
) {

    // ============================================================================
    // STUDENT OPERATIONS
    // ============================================================================

    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)

    suspend fun loginStudent(email: String, password: String): Student? =
        studentDao.login(email, password)

    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()

    suspend fun registerStudent(student: Student): Long =
        studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun getStudentCount(): Int = studentDao.getStudentCount()

    // ============================================================================
    // COUNSELOR OPERATIONS
    // ============================================================================

    fun getCounselorById(id: Long): Flow<Counselor?> = counselorDao.getCounselorById(id)

    suspend fun loginCounselor(email: String, password: String): Counselor? =
        counselorDao.login(email, password)

    fun getAllCounselors(): Flow<List<Counselor>> = counselorDao.getAllCounselors()

    suspend fun registerCounselor(counselor: Counselor): Long =
        counselorDao.insertCounselor(counselor)

    // ============================================================================
    // MOOD OPERATIONS
    // ============================================================================

    fun getMoodsByStudent(studentId: Long): Flow<List<Mood>> =
        moodDao.getMoodsByStudent(studentId)

    fun getRecentMoods(studentId: Long, limit: Int = 7): Flow<List<Mood>> =
        moodDao.getRecentMoods(studentId, limit)

    suspend fun getAverageMood(studentId: Long, daysBack: Int = 7): Double? {
        val startTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return moodDao.getAverageMoodSince(studentId, startTime)
    }

    suspend fun insertMood(mood: Mood): Long = moodDao.insertMood(mood)

    // ============================================================================
    // JOURNAL OPERATIONS
    // ============================================================================

    fun getJournalsByStudent(studentId: Long): Flow<List<Journal>> =
        journalDao.getJournalsByStudent(studentId)

    fun getRecentJournals(studentId: Long, limit: Int = 5): Flow<List<Journal>> =
        journalDao.getRecentJournals(studentId, limit)

    suspend fun getJournalById(id: Long): Journal? = journalDao.getJournalById(id)

    suspend fun insertJournal(journal: Journal): Long = journalDao.insertJournal(journal)

    suspend fun updateJournal(journal: Journal) = journalDao.updateJournal(journal)

    suspend fun deleteJournal(journal: Journal) = journalDao.deleteJournal(journal)

    // ============================================================================
    // ACTIVITY OPERATIONS
    // ============================================================================

    fun getActivitiesByStudent(studentId: Long): Flow<List<Activity>> =
        activityDao.getActivitiesByStudent(studentId)

    fun getRecentActivities(studentId: Long, limit: Int = 7): Flow<List<Activity>> =
        activityDao.getRecentActivities(studentId, limit)

    suspend fun getAverageSteps(studentId: Long, daysBack: Int = 7): Double? {
        val startDate = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return activityDao.getAverageStepsSince(studentId, startDate)
    }

    suspend fun getAverageSleep(studentId: Long, daysBack: Int = 7): Double? {
        val startDate = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return activityDao.getAverageSleepSince(studentId, startDate)
    }

    suspend fun insertActivity(activity: Activity): Long = activityDao.insertActivity(activity)

    // ============================================================================
    // ATTENDANCE OPERATIONS
    // ============================================================================

    fun getAttendanceByStudent(studentId: Long): Flow<List<Attendance>> =
        attendanceDao.getAttendanceByStudent(studentId)

    fun getRecentAttendance(studentId: Long, limit: Int = 4): Flow<List<Attendance>> =
        attendanceDao.getRecentAttendance(studentId, limit)

    suspend fun getAverageAttendance(studentId: Long): Double? =
        attendanceDao.getAverageAttendance(studentId)

    suspend fun insertAttendance(attendance: Attendance): Long =
        attendanceDao.insertAttendance(attendance)

    // ============================================================================
    // MEETING OPERATIONS
    // ============================================================================

    fun getMeetingsByStudent(studentId: Long): Flow<List<Meeting>> =
        meetingDao.getMeetingsByStudent(studentId)

    fun getMeetingsByCounselor(counselorId: Long): Flow<List<Meeting>> =
        meetingDao.getMeetingsByCounselor(counselorId)

    suspend fun scheduleMeeting(meeting: Meeting): Long = meetingDao.insertMeeting(meeting)

    suspend fun updateMeetingStatus(meetingId: Long, status: String) =
        meetingDao.updateMeetingStatus(meetingId, status)

    // ============================================================================
    // NOTIFICATION OPERATIONS
    // ============================================================================

    fun getNotificationsByUser(userId: Long, userType: String): Flow<List<Notification>> =
        notificationDao.getNotificationsByUser(userId, userType)

    fun getUnreadCount(userId: Long, userType: String): Flow<Int> =
        notificationDao.getUnreadCount(userId, userType)

    suspend fun insertNotification(notification: Notification): Long =
        notificationDao.insertNotification(notification)

    suspend fun markNotificationAsRead(notificationId: Long) =
        notificationDao.markAsRead(notificationId)

    suspend fun markAllNotificationsAsRead(userId: Long, userType: String) =
        notificationDao.markAllAsRead(userId, userType)

    suspend fun deleteNotification(notification: Notification) =
        notificationDao.deleteNotification(notification)

    // Create notification when counselor schedules meeting
    suspend fun scheduleMeetingAndNotify(studentId: Long, counselorId: Long): Long {
        // Create meeting
        val meetingId = meetingDao.insertMeeting(
            Meeting(
                studentId = studentId,
                counselorId = counselorId,
                status = "scheduled",
                scheduledAt = System.currentTimeMillis()
            )
        )

        // Get student name
        // Note: We'll need to handle this with proper student lookup in ViewModel

        // Create notification for student
        notificationDao.insertNotification(
            Notification(
                userId = studentId,
                userType = "student",
                title = "Counseling Session Scheduled",
                message = "A counselor has scheduled a session with you. You will be contacted soon.",
                referenceId = counselorId,
                isRead = false
            )
        )

        return meetingId
    }

    // Create notification when student requests meeting
    suspend fun requestMeetingAndNotify(studentId: Long, allCounselorIds: List<Long>): Long {
        // Create meeting request
        val meetingId = meetingDao.insertMeeting(
            Meeting(
                studentId = studentId,
                counselorId = null,
                status = "scheduled",
                scheduledAt = System.currentTimeMillis()
            )
        )

        // Notify all counselors
        allCounselorIds.forEach { counselorId ->
            notificationDao.insertNotification(
                Notification(
                    userId = counselorId,
                    userType = "counselor",
                    title = "New Meeting Request",
                    message = "A student has requested a counseling session.",
                    referenceId = studentId,
                    isRead = false
                )
            )
        }

        return meetingId
    }

    // ============================================================================
    // RISK CALCULATION (from Flask app logic)
    // ============================================================================

    suspend fun calculateRiskScore(studentId: Long): RiskAssessment {
        val student = studentDao.getStudentByEmail("") // We'll get it from flow in VM
        val avgAttendance = getAverageAttendance(studentId) ?: 100.0
        val avgMood = getAverageMood(studentId, 7) ?: 7.0

        var riskScore = 0
        val factors = mutableMapOf<String, String>()

        // Get student for CGPA - we'll need to handle this in ViewModel
        // For now, just attendance and mood

        // Factor 1: Attendance (30 points)
        when {
            avgAttendance < 70 -> {
                riskScore += 30
                factors["attendance"] =
                    "Critical - ${String.format("%.1f", avgAttendance)}% attendance"
            }

            avgAttendance < 75 -> {
                riskScore += 20
                factors["attendance"] =
                    "Concerning - ${String.format("%.1f", avgAttendance)}% attendance"
            }

            avgAttendance < 85 -> {
                riskScore += 10
                factors["attendance"] =
                    "Below target - ${String.format("%.1f", avgAttendance)}% attendance"
            }

            else -> {
                factors["attendance"] = "Good - ${String.format("%.1f", avgAttendance)}% attendance"
            }
        }

        // Factor 2: Mental Health/Mood (20 points)
        when {
            avgMood < 4 -> {
                riskScore += 20
                factors["mental_health"] =
                    "Critical - Low mood (avg: ${String.format("%.1f", avgMood)}/10)"
            }

            avgMood < 6 -> {
                riskScore += 15
                factors["mental_health"] =
                    "Concerning mood (avg: ${String.format("%.1f", avgMood)}/10)"
            }

            avgMood < 7 -> {
                riskScore += 8
                factors["mental_health"] = "Fair mood (avg: ${String.format("%.1f", avgMood)}/10)"
            }

            else -> {
                factors["mental_health"] = "Good mood (avg: ${String.format("%.1f", avgMood)}/10)"
            }
        }

        val riskLevel = when {
            riskScore >= 50 -> "high"
            riskScore >= 30 -> "moderate"
            else -> "low"
        }

        return RiskAssessment(riskLevel, riskScore, factors)
    }
}

data class RiskAssessment(
    val level: String,
    val score: Int,
    val factors: Map<String, String>
)
