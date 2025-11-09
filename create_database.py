import sqlite3
import os
from datetime import datetime, timedelta
import random

def create_database(db_path='instance/ira.db'):
    """Create the database and initialize tables with sample data"""
    
    # Create directory if it doesn't exist
    db_dir = os.path.dirname(db_path)
    if db_dir and not os.path.exists(db_dir):
        os.makedirs(db_dir)
    
    # Connect to database
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # Create students table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS students (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        roll_number TEXT UNIQUE NOT NULL,
        department TEXT NOT NULL,
        semester INTEGER NOT NULL,
        cgpa REAL DEFAULT 0.0,
        fee_pending BOOLEAN DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
    ''')
    
    # Create counselors table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS counselors (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        phone TEXT,
        employee_id TEXT UNIQUE,
        license_number TEXT,
        specialization TEXT,
        qualifications TEXT,
        experience_years INTEGER DEFAULT 0,
        department TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
    ''')
    
    # Create moods table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS moods (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        student_id INTEGER NOT NULL,
        mood_score INTEGER NOT NULL,
        notes TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (student_id) REFERENCES students(id)
    )
    ''')
    
    # Create journals table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS journals (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        student_id INTEGER NOT NULL,
        title TEXT NOT NULL,
        content TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (student_id) REFERENCES students(id)
    )
    ''')
    
    # Create activities table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS activities (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        student_id INTEGER NOT NULL,
        date DATE NOT NULL,
        steps INTEGER DEFAULT 0,
        sleep_hours REAL DEFAULT 0.0,
        exercise_minutes INTEGER DEFAULT 0,
        FOREIGN KEY (student_id) REFERENCES students(id)
    )
    ''')
    
    # Create attendance table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS attendance (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        student_id INTEGER NOT NULL,
        month TEXT NOT NULL,
        attendance_percentage REAL NOT NULL,
        total_classes INTEGER NOT NULL,
        attended_classes INTEGER NOT NULL,
        FOREIGN KEY (student_id) REFERENCES students(id)
    )
    ''')
    
    # Create meetings table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS meetings (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        student_id INTEGER NOT NULL,
        counselor_id INTEGER,
        status TEXT DEFAULT 'scheduled',
        notes TEXT,
        scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (student_id) REFERENCES students(id),
        FOREIGN KEY (counselor_id) REFERENCES counselors(id)
    )
    ''')
    
    # Create notifications table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS notifications (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        user_type TEXT NOT NULL,
        title TEXT NOT NULL,
        message TEXT NOT NULL,
        link TEXT,
        reference_id INTEGER,
        is_read BOOLEAN DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
    ''')
    
    # Insert sample counselor (password: counselor123)
    cursor.execute('''
    INSERT OR IGNORE INTO counselors (name, email, password, phone, employee_id, license_number, specialization, qualifications, experience_years, department)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', ('Dr. Sarah Johnson', 'counselor@ira.edu', 'counselor123', '9876543210', 'EMP001', 'LIC123456', 'Student Counseling', "Master's in Psychology", 8, 'Student Wellness Center'))
    
    # Insert sample students (password: student123 for all)
    students_data = [
        ('Aarav Sharma', 'aarav@student.edu', 'student123', 'CS001', 'Computer Science', 3, 8.2, 0),
        ('Priya Patel', 'priya@student.edu', 'student123', 'CS002', 'Computer Science', 3, 6.5, 1),
        ('Rohan Kumar', 'rohan@student.edu', 'student123', 'EE001', 'Electrical Engineering', 2, 7.8, 0),
        ('Ananya Singh', 'ananya@student.edu', 'student123', 'ME001', 'Mechanical Engineering', 4, 5.9, 1),
        ('Vikram Mehta', 'vikram@student.edu', 'student123', 'CS003', 'Computer Science', 3, 9.1, 0),
        ('Neha Reddy', 'neha@student.edu', 'student123', 'EC001', 'Electronics', 2, 6.2, 0),
        ('Arjun Gupta', 'arjun@student.edu', 'student123', 'CS004', 'Computer Science', 3, 7.5, 1),
        ('Isha Verma', 'isha@student.edu', 'student123', 'ME002', 'Mechanical Engineering', 4, 8.7, 0),
        ('Rahul Desai', 'rahul@student.edu', 'student123', 'CS005', 'Computer Science', 3, 5.5, 1),  # HIGH RISK: Low CGPA + Fee pending
    ]
    
    for student in students_data:
        cursor.execute('''
        INSERT OR IGNORE INTO students (name, email, password, roll_number, department, semester, cgpa, fee_pending)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ''', student)
    
    conn.commit()
    
    # Get student IDs
    cursor.execute('SELECT id FROM students')
    student_ids = [row[0] for row in cursor.fetchall()]
    
    # Insert sample mood data for past 7 days
    for student_id in student_ids:
        for i in range(7):
            date = datetime.now() - timedelta(days=i)
            mood_score = random.randint(3, 10) if student_id not in [2, 4, 6, 9] else random.randint(2, 6)
            cursor.execute('''
            INSERT INTO moods (student_id, mood_score, notes, created_at)
            VALUES (?, ?, ?, ?)
            ''', (student_id, mood_score, 'Daily mood check', date))
    
    # Insert sample activity data
    for student_id in student_ids:
        for i in range(7):
            date = (datetime.now() - timedelta(days=i)).strftime('%Y-%m-%d')
            
            # High risk students have lower activity levels
            if student_id in [2, 4, 6, 9]:
                steps = random.randint(2000, 4000)  # Low activity for high-risk students
                sleep_hours = random.uniform(5.0, 6.5)  # Poor sleep
                exercise_minutes = random.randint(0, 20)  # Minimal exercise
            else:
                steps = random.randint(5000, 10000)  # Normal activity
                sleep_hours = random.uniform(6.5, 9.0)  # Good sleep
                exercise_minutes = random.randint(20, 60)  # Regular exercise
            
            cursor.execute('''
            INSERT INTO activities (student_id, date, steps, sleep_hours, exercise_minutes)
            VALUES (?, ?, ?, ?, ?)
            ''', (student_id, date, steps, round(sleep_hours, 1), exercise_minutes))
    
    # Insert sample attendance data
    months = ['January', 'February', 'March', 'April']
    for student_id in student_ids:
        for month in months:
            # High risk students have lower attendance
            if student_id in [2, 4, 6, 9]:
                attendance_pct = random.uniform(40, 60)
            else:
                attendance_pct = random.uniform(80, 95)
            
            total_classes = random.randint(20, 30)
            attended_classes = int((attendance_pct / 100) * total_classes)
            
            cursor.execute('''
            INSERT INTO attendance (student_id, month, attendance_percentage, total_classes, attended_classes)
            VALUES (?, ?, ?, ?, ?)
            ''', (student_id, month, round(attendance_pct, 1), total_classes, attended_classes))
    
    # Insert some sample journals
    journal_entries = [
        (1, 'Excited about Project', 'Working on a new machine learning project. Feeling motivated!'),
        (2, 'Struggling with Assignments', 'Finding it hard to keep up with assignments. Feeling overwhelmed.'),
        (3, 'Good Week', 'Had a productive week. Completed all labs on time.'),
        (4, 'Stress About Exams', 'Mid-terms are approaching and I feel unprepared.'),
    ]
    
    for entry in journal_entries:
        cursor.execute('''
        INSERT INTO journals (student_id, title, content)
        VALUES (?, ?, ?)
        ''', entry)
    
    # Insert some sample meetings
    for student_id in [2, 4, 6, 9]:
        cursor.execute('''
        INSERT INTO meetings (student_id, counselor_id, status)
        VALUES (?, 1, 'scheduled')
        ''', (student_id,))
    
    conn.commit()
    conn.close()
    
    print(f"✅ Database created successfully at {db_path}!")
    print("📊 Sample data inserted")
    print("\n🔐 Login Credentials:")
    print("\n👨‍🎓 Student Login:")
    print("   Email: aarav@student.edu")
    print("   Password: student123")
    print("\n👨‍⚕️ Counselor Login:")
    print("   Email: counselor@ira.edu")
    print("   Password: counselor123")

if __name__ == '__main__':
    # Check if RENDER environment variable is set
    db_path = '/tmp/ira.db' if os.getenv('RENDER') else 'instance/ira.db'
    create_database(db_path)
