package com.example.ira.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ira.data.local.dao.*
import com.example.ira.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

@Database(
    entities = [
        Student::class,
        Counselor::class,
        Mood::class,
        Journal::class,
        Activity::class,
        Attendance::class,
        Meeting::class,
        Notification::class
    ],
    version = 3,
    exportSchema = false
)
abstract class IRADatabase : RoomDatabase() {
    
    abstract fun studentDao(): StudentDao
    abstract fun counselorDao(): CounselorDao
    abstract fun moodDao(): MoodDao
    abstract fun journalDao(): JournalDao
    abstract fun activityDao(): ActivityDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun meetingDao(): MeetingDao
    abstract fun notificationDao(): NotificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: IRADatabase? = null
        
        fun getDatabase(context: Context, scope: CoroutineScope): IRADatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IRADatabase::class.java,
                    "ira_database"
                )
                    .fallbackToDestructiveMigration() // For development - removes this in production
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
            
            suspend fun populateDatabase(database: IRADatabase) {
                val studentDao = database.studentDao()
                val counselorDao = database.counselorDao()
                val moodDao = database.moodDao()
                val activityDao = database.activityDao()
                val attendanceDao = database.attendanceDao()
                val meetingDao = database.meetingDao()
                val notificationDao = database.notificationDao()
                
                // Insert sample students
                val student1Id = studentDao.insertStudent(
                    Student(
                        name = "Aarav Sharma",
                        email = "aarav@student.edu",
                        password = "student123",
                        rollNumber = "CS2021001",
                        department = "Computer Science",
                        semester = 6,
                        cgpa = 7.8,
                        feePending = false
                    )
                )
                
                val student2Id = studentDao.insertStudent(
                    Student(
                        name = "Priya Patel",
                        email = "priya@student.edu",
                        password = "student123",
                        rollNumber = "CS2021002",
                        department = "Computer Science",
                        semester = 6,
                        cgpa = 6.2,
                        feePending = true
                    )
                )
                
                val student3Id = studentDao.insertStudent(
                    Student(
                        name = "Rohan Kumar",
                        email = "rohan@student.edu",
                        password = "student123",
                        rollNumber = "EC2021003",
                        department = "Electronics",
                        semester = 4,
                        cgpa = 8.5,
                        feePending = false
                    )
                )

                // Add high-risk student for counselor dashboard demo
                val student4Id = studentDao.insertStudent(
                    Student(
                        name = "Rahul Sharma",
                        email = "rahul@student.edu",
                        password = "student123",
                        rollNumber = "21BCS101",
                        department = "Computer Science",
                        semester = 6,
                        cgpa = 6.2,
                        feePending = false
                    )
                )

                val student5Id = studentDao.insertStudent(
                    Student(
                        name = "Sneha Kumar",
                        email = "sneha@student.edu",
                        password = "student123",
                        rollNumber = "21BCS104",
                        department = "Computer Science",
                        semester = 6,
                        cgpa = 6.8,
                        feePending = false
                    )
                )

                // Insert sample counselor
                val counselorId = counselorDao.insertCounselor(
                    Counselor(
                        name = "Dr. Sarah Johnson",
                        email = "counselor@ira.edu",
                        password = "counselor123",
                        phone = "+91 98765 43210",
                        employeeId = "EMP001",
                        licenseNumber = "LIC12345",
                        specialization = "Clinical Psychology",
                        qualifications = "PhD in Clinical Psychology, M.A. in Counseling",
                        experienceYears = 10,
                        department = "Student Wellness"
                    )
                )

                // Add sample notifications for students
                notificationDao.insertNotification(
                    Notification(
                        userId = student1Id,
                        userType = "student",
                        title = "Counseling Session Scheduled",
                        message = "A counselor has scheduled a session with you. You will be contacted soon.",
                        referenceId = counselorId,
                        isRead = false
                    )
                )

                notificationDao.insertNotification(
                    Notification(
                        userId = student4Id,
                        userType = "student",
                        title = "Counseling Session Scheduled",
                        message = "A counselor has scheduled a session with you. You will be contacted soon.",
                        referenceId = counselorId,
                        isRead = false
                    )
                )

                // Add sample notifications for counselor
                notificationDao.insertNotification(
                    Notification(
                        userId = counselorId,
                        userType = "counselor",
                        title = "New Meeting Request",
                        message = "Rahul Sharma (21BCS101) has requested a counseling session.",
                        referenceId = student4Id,
                        isRead = false
                    )
                )

                notificationDao.insertNotification(
                    Notification(
                        userId = counselorId,
                        userType = "counselor",
                        title = "New Meeting Request",
                        message = "Sneha Kumar (21BCS104) has requested a counseling session.",
                        referenceId = student5Id,
                        isRead = false
                    )
                )

                // Add sample mood data for last 7 days
                val now = System.currentTimeMillis()
                val dayInMillis = 24 * 60 * 60 * 1000L
                
                for (i in 0..6) {
                    moodDao.insertMood(
                        Mood(
                            studentId = student1Id,
                            moodScore = Random.nextInt(6, 10),
                            notes = "Feeling ${listOf("great", "good", "okay", "stressed").random()}",
                            createdAt = now - (i * dayInMillis)
                        )
                    )
                    
                    moodDao.insertMood(
                        Mood(
                            studentId = student2Id,
                            moodScore = Random.nextInt(4, 8),
                            notes = "Today was ${listOf("challenging", "manageable", "tough").random()}",
                            createdAt = now - (i * dayInMillis)
                        )
                    )

                    // Rahul Sharma - high risk with declining mood
                    moodDao.insertMood(
                        Mood(
                            studentId = student4Id,
                            moodScore = listOf(7, 6, 5, 4, 4, 3, 4)[i],
                            notes = "Feeling ${
                                listOf(
                                    "stressed",
                                    "overwhelmed",
                                    "anxious",
                                    "tired"
                                ).random()
                            }",
                            createdAt = now - (i * dayInMillis)
                        )
                    )

                    // Sneha Kumar - high risk
                    moodDao.insertMood(
                        Mood(
                            studentId = student5Id,
                            moodScore = listOf(6, 5, 6, 5, 4, 4, 3)[i],
                            notes = "Struggling with ${
                                listOf(
                                    "classes",
                                    "assignments",
                                    "exams"
                                ).random()
                            }",
                            createdAt = now - (i * dayInMillis)
                        )
                    )
                }
                
                // Add sample activity data
                for (i in 0..6) {
                    activityDao.insertActivity(
                        Activity(
                            studentId = student1Id,
                            steps = Random.nextInt(5000, 12001),
                            sleepHours = Random.nextDouble(5.5, 8.5),
                            exerciseMinutes = Random.nextInt(0, 61),
                            date = now - (i * dayInMillis)
                        )
                    )
                    
                    activityDao.insertActivity(
                        Activity(
                            studentId = student2Id,
                            steps = Random.nextInt(3000, 8001),
                            sleepHours = Random.nextDouble(4.0, 7.0),
                            exerciseMinutes = Random.nextInt(0, 31),
                            date = now - (i * dayInMillis)
                        )
                    )

                    // Rahul Sharma - low activity, poor sleep
                    activityDao.insertActivity(
                        Activity(
                            studentId = student4Id,
                            steps = Random.nextInt(2000, 5001),
                            sleepHours = Random.nextDouble(4.5, 6.5),
                            exerciseMinutes = Random.nextInt(0, 20),
                            date = now - (i * dayInMillis)
                        )
                    )

                    // Sneha Kumar - moderate activity
                    activityDao.insertActivity(
                        Activity(
                            studentId = student5Id,
                            steps = Random.nextInt(3500, 6001),
                            sleepHours = Random.nextDouble(5.0, 7.0),
                            exerciseMinutes = Random.nextInt(10, 30),
                            date = now - (i * dayInMillis)
                        )
                    )
                }
                
                // Add sample attendance data
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val months = listOf("January", "February", "March", "April")
                
                months.forEachIndexed { index, month ->
                    attendanceDao.insertAttendance(
                        Attendance(
                            studentId = student1Id,
                            month = month,
                            year = currentYear,
                            attendancePercentage = Random.nextDouble(85.0, 95.0)
                        )
                    )
                    
                    attendanceDao.insertAttendance(
                        Attendance(
                            studentId = student2Id,
                            month = month,
                            year = currentYear,
                            attendancePercentage = Random.nextDouble(65.0, 75.0)
                        )
                    )
                    
                    attendanceDao.insertAttendance(
                        Attendance(
                            studentId = student3Id,
                            month = month,
                            year = currentYear,
                            attendancePercentage = Random.nextDouble(90.0, 98.0)
                        )
                    )

                    // Rahul Sharma - critical attendance
                    attendanceDao.insertAttendance(
                        Attendance(
                            studentId = student4Id,
                            month = month,
                            year = currentYear,
                            attendancePercentage = Random.nextDouble(58.0, 67.0)
                        )
                    )

                    // Sneha Kumar - critical attendance
                    attendanceDao.insertAttendance(
                        Attendance(
                            studentId = student5Id,
                            month = month,
                            year = currentYear,
                            attendancePercentage = Random.nextDouble(65.0, 72.0)
                        )
                    )
                }
            }
        }
    }
}
