from flask import Flask, render_template, request, redirect, url_for, session, jsonify, flash, Response, stream_with_context
import sqlite3
import os
from datetime import datetime, timedelta
from dotenv import load_dotenv
import google.generativeai as genai
import json
import threading

load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv('SECRET_KEY', 'ira_secret_key')

# ✅ Use /tmp on Render to avoid permission issues
db_path = '/tmp/ira.db' if os.getenv('RENDER') else 'instance/ira.db'
app.config['DATABASE'] = db_path

# ✅ Ensure directory exists
db_dir = os.path.dirname(db_path)
if db_dir:
    os.makedirs(db_dir, exist_ok=True)

# Initialize AI models at startup
emotion_analyzer = None
dropout_predictor = None
ai_models_loading = True  # Flag to track loading status
ai_models_enabled = not os.getenv('DISABLE_AI_MODELS', '').lower() == 'true'  # Can disable via env var

# Configure Gemini API
GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')
if GEMINI_API_KEY and GEMINI_API_KEY != 'your_gemini_api_key_here':
    genai.configure(api_key=GEMINI_API_KEY)
    print("✅ Gemini API configured successfully")
else:
    print("⚠️ Gemini API key not found. Chatbot will use fallback responses.")

def initialize_ai_models():
    """Initialize AI models at application startup"""
    global emotion_analyzer, dropout_predictor, ai_models_loading
    
    if not ai_models_enabled:
        print("⚠️ AI models disabled via environment variable")
        ai_models_loading = False
        return False
    
    try:
        print("Initializing AI models in background...")
        
        # Initialize emotion analyzer
        try:
            from ai_models.emotion_model import EmotionAnalyzer
            emotion_analyzer = EmotionAnalyzer()
            print("✅ Emotion analyzer loaded successfully")
        except Exception as e:
            print(f"⚠️ Could not load emotion analyzer: {e}")
            print("App will continue without emotion analysis")
        
        # Initialize dropout risk predictor
        try:
            from ai_models.tabular_model import DropoutRiskPredictor
            dropout_predictor = DropoutRiskPredictor()
            print("✅ Dropout risk predictor loaded successfully")
        except Exception as e:
            print(f"⚠️ Could not load dropout predictor: {e}")
            print("App will continue without ML-based dropout prediction")
        
        ai_models_loading = False
        
        if emotion_analyzer or dropout_predictor:
            print("AI models initialized successfully!")
            return True
        else:
            print("⚠️ No AI models loaded, but app will function normally")
            return False
            
    except Exception as e:
        print(f"⚠️ Error initializing AI models: {e}")
        print("The application will continue with basic functionality.")
        ai_models_loading = False
        return False

def initialize_ai_models_background():
    """Start AI model initialization in background thread"""
    thread = threading.Thread(target=initialize_ai_models, daemon=True)
    thread.start()
    print("AI models loading in background (app starting immediately)...")

def get_db():
    """Connect to the database"""
    conn = sqlite3.connect(app.config['DATABASE'])
    conn.row_factory = sqlite3.Row
    return conn

def ensure_database_exists():
    """Ensure database exists with all tables and sample data"""
    print("=" * 80)
    print("DATABASE INITIALIZATION STARTING")
    print(f"Database path: {app.config['DATABASE']}")
    print(f"RENDER env var: {os.getenv('RENDER')}")
    print(f"Database file exists: {os.path.exists(app.config['DATABASE'])}")
    print("=" * 80)
    
    # On Render, ALWAYS recreate the database to ensure tables exist
    if os.getenv('RENDER'):
        print("Render environment detected - ensuring database is properly initialized...")
        if os.path.exists(app.config['DATABASE']):
            # Verify tables exist
            try:
                conn = get_db()
                cursor = conn.cursor()
                cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='students'")
                table_exists = cursor.fetchone()
                conn.close()
                
                if not table_exists:
                    print("Database file exists but tables are missing! Recreating...")
                    os.remove(app.config['DATABASE'])
                else:
                    print("Database verified - all tables exist!")
                    print("=" * 80)
                    return
            except Exception as e:
                print(f"Error verifying database: {e}")
                import traceback
                traceback.print_exc()
                if os.path.exists(app.config['DATABASE']):
                    print("Removing corrupted database file...")
                    os.remove(app.config['DATABASE'])
        
        # Create fresh database
        print(f"Creating new database at {app.config['DATABASE']}")
        print(f"Current working directory: {os.getcwd()}")
        print(f"create_database.py exists: {os.path.exists('create_database.py')}")
        
        try:
            print("Importing create_database function...")
            from create_database import create_database
            print("Import successful, calling create_database()...")
            create_database(app.config['DATABASE'])
            print("Database created successfully with sample data!")
            
            # Verify it worked
            if os.path.exists(app.config['DATABASE']):
                print(f"Database file created successfully at {app.config['DATABASE']}")
                conn = get_db()
                cursor = conn.cursor()
                cursor.execute("SELECT COUNT(*) as count FROM students")
                count = cursor.fetchone()['count']
                conn.close()
                print(f"Verified: {count} students in database")
            else:
                print("ERROR: Database file was not created!")
                raise Exception("Database file was not created")
                
        except Exception as e:
            print(f"CRITICAL ERROR creating database: {e}")
            import traceback
            traceback.print_exc()
            raise
    else:
        # Local environment - only create if missing
        if not os.path.exists(app.config['DATABASE']):
            print(f"Creating new database at {app.config['DATABASE']}")
            try:
                from create_database import create_database
                create_database(app.config['DATABASE'])
                print("Database created successfully with sample data!")
            except Exception as e:
                print(f"Error creating database: {e}")
                import traceback
                traceback.print_exc()
                raise
        else:
            print(f"Database found at {app.config['DATABASE']}")
    
    print("=" * 80)
    print("DATABASE INITIALIZATION COMPLETED")
    print("=" * 80)

# Initialize database NOW
print("\n" + "=" * 80)
print("ATTEMPTING DATABASE INITIALIZATION")
print("=" * 80)
try:
    ensure_database_exists()
except Exception as e:
    print(f"\n" + "=" * 80)
    print("FATAL ERROR DURING DATABASE INITIALIZATION")
    print("=" * 80)
    print(f"Error: {e}")
    import traceback
    traceback.print_exc()
    print(f"=" * 80 + "\n")
    # Don't raise - let app start and show error on first request

def init_db():
    """Initialize database with basic schema"""
    conn = sqlite3.connect(app.config['DATABASE'])
    c = conn.cursor()
    
    # Create all tables
    c.execute('''CREATE TABLE IF NOT EXISTS students (
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
                );''')
    
    c.execute('''CREATE TABLE IF NOT EXISTS counselors (
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
                );''')
    
    c.execute('''CREATE TABLE IF NOT EXISTS moods (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    mood_score INTEGER NOT NULL,
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES students(id)
                );''')
    
    c.execute('''CREATE TABLE IF NOT EXISTS journals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES students(id)
                );''')
    
    c.execute('''CREATE TABLE IF NOT EXISTS activities (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    date DATE NOT NULL,
                    steps INTEGER DEFAULT 0,
                    sleep_hours REAL DEFAULT 0.0,
                    exercise_minutes INTEGER DEFAULT 0,
                    FOREIGN KEY (student_id) REFERENCES students(id)
                );''')
    
    c.execute('''CREATE TABLE IF NOT EXISTS attendance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    month TEXT NOT NULL,
                    attendance_percentage REAL NOT NULL,
                    total_classes INTEGER NOT NULL,
                    attended_classes INTEGER NOT NULL,
                    FOREIGN KEY (student_id) REFERENCES students(id)
                );''')
    
    c.execute('''CREATE TABLE IF NOT EXISTS meetings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    counselor_id INTEGER,
                    status TEXT DEFAULT 'scheduled',
                    notes TEXT,
                    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES students(id),
                    FOREIGN KEY (counselor_id) REFERENCES counselors(id)
                );''')
    
    c.execute('''CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    user_type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    message TEXT NOT NULL,
                    link TEXT,
                    reference_id INTEGER,
                    is_read BOOLEAN DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );''')
    
    conn.commit()
    conn.close()
    print(f"✅ Database initialized at {app.config['DATABASE']}")

def calculate_risk_score(student_id):
    """
    Calculate dropout risk score based on multiple factors
    Returns: (risk_level, risk_score, factors)
    risk_level: 'high', 'moderate', or 'low'
    risk_score: 0-100
    factors: dict of contributing factors
    """
    conn = get_db()
    cursor = conn.cursor()
    
    # Get student data
    cursor.execute('SELECT * FROM students WHERE id = ?', (student_id,))
    student = cursor.fetchone()
    
    if not student:
        return 'low', 0, {}
    
    risk_score = 0
    factors = {}
    
    # Factor 1: CGPA (30 points)
    cgpa = student['cgpa']
    if cgpa < 6.0:
        risk_score += 30
        factors['academics'] = 'Critical - CGPA below 6.0'
    elif cgpa < 7.0:
        risk_score += 20
        factors['academics'] = 'Concerning - CGPA below 7.0'
    elif cgpa < 8.0:
        risk_score += 10
        factors['academics'] = 'Below average'
    else:
        factors['academics'] = 'Good performance'
    
    # Factor 2: Attendance (30 points)
    cursor.execute('''
        SELECT AVG(attendance_percentage) as avg_attendance 
        FROM attendance 
        WHERE student_id = ?
    ''', (student_id,))
    attendance_data = cursor.fetchone()
    avg_attendance = attendance_data['avg_attendance'] if attendance_data and attendance_data['avg_attendance'] is not None else 100
    
    if avg_attendance < 70:
        risk_score += 30
        factors['attendance'] = f'Critical - {avg_attendance:.1f}% attendance'
    elif avg_attendance < 75:
        risk_score += 20
        factors['attendance'] = f'Concerning - {avg_attendance:.1f}% attendance'
    elif avg_attendance < 85:
        risk_score += 10
        factors['attendance'] = f'Below target - {avg_attendance:.1f}% attendance'
    else:
        factors['attendance'] = f'Good - {avg_attendance:.1f}% attendance'
    
    # Factor 3: Fee Pending (20 points)
    if student['fee_pending']:
        risk_score += 20
        factors['fees'] = 'Fee payment pending'
    else:
        factors['fees'] = 'No pending fees'
    
    # Factor 4: Mental Health/Mood (20 points)
    cursor.execute('''
        SELECT AVG(mood_score) as avg_mood 
        FROM moods 
        WHERE student_id = ? 
        AND created_at >= datetime('now', '-7 days')
    ''', (student_id,))
    mood_data = cursor.fetchone()
    avg_mood = mood_data['avg_mood'] if mood_data and mood_data['avg_mood'] else 7
    
    if avg_mood < 4:
        risk_score += 20
        factors['mental_health'] = f'Critical - Low mood (avg: {avg_mood:.1f}/10)'
    elif avg_mood < 6:
        risk_score += 15
        factors['mental_health'] = f'Concerning mood (avg: {avg_mood:.1f}/10)'
    elif avg_mood < 7:
        risk_score += 8
        factors['mental_health'] = f'Fair mood (avg: {avg_mood:.1f}/10)'
    else:
        factors['mental_health'] = f'Good mood (avg: {avg_mood:.1f}/10)'
    
    conn.close()
    
    # Determine risk level
    if risk_score >= 50:
        risk_level = 'high'
    elif risk_score >= 30:
        risk_level = 'moderate'
    else:
        risk_level = 'low'
    
    return risk_level, risk_score, factors

def get_wellness_tips(risk_level, factors):
    """Generate personalized wellness tips based on risk factors"""
    tips = []
    
    if 'Critical' in factors.get('academics', ''):
        tips.append({
            'icon': '📚',
            'title': 'Academic Support',
            'text': 'Schedule tutoring sessions and meet with your professors during office hours.'
        })
    
    if 'Critical' in factors.get('attendance', '') or 'Concerning' in factors.get('attendance', ''):
        tips.append({
            'icon': '📅',
            'title': 'Improve Attendance',
            'text': 'Set daily reminders for classes and try to maintain at least 75% attendance.'
        })
    
    if 'pending' in factors.get('fees', ''):
        tips.append({
            'icon': '💰',
            'title': 'Fee Payment',
            'text': 'Contact the accounts office to discuss payment plans or scholarship opportunities.'
        })
    
    if 'Critical' in factors.get('mental_health', '') or 'Concerning' in factors.get('mental_health', ''):
        tips.append({
            'icon': '🧘',
            'title': 'Mental Wellness',
            'text': 'Practice mindfulness, maintain a regular sleep schedule, and consider talking to a counselor.'
        })
    
    # Add general tips
    if risk_level == 'low':
        tips.append({
            'icon': '⭐',
            'title': 'Keep It Up!',
            'text': "You're doing great! Maintain your routine and stay engaged with your studies."
        })
    
    tips.append({
        'icon': '🏃',
        'title': 'Stay Active',
        'text': 'Regular physical activity can improve focus and reduce stress. Aim for 30 minutes daily.'
    })
    
    tips.append({
        'icon': '💤',
        'title': 'Quality Sleep',
        'text': 'Aim for 7-8 hours of sleep each night for better cognitive function and mood.'
    })
    
    return tips

# System prompt for Ira.ai
SYSTEM_PROMPT = """You are Ira.ai, an AI wellbeing companion built for students. Your goal is to support their emotional, academic, and physical wellbeing.

Respond empathetically, with warmth and understanding.
Keep responses concise, human-like, and natural — like a supportive senior or mentor.
Never sound robotic or overly formal.
When asked academic or motivational questions, offer guidance while maintaining a friendly tone.
If students share stress, anxiety, or confusion, respond gently and offer actionable suggestions (like relaxation tips or time management help).
Always maintain a positive, respectful, and privacy-conscious tone.
Keep responses under 150 words unless specifically asked for more detail."""

@app.route('/')
def index():
    """Home page - shows landing page or redirects if logged in"""
    if 'user_id' in session:
        if session.get('user_type') == 'student':
            return redirect(url_for('student_dashboard', id=session['user_id']))
        elif session.get('user_type') == 'counselor':
            return redirect(url_for('counselor_dashboard'))
    return render_template('landing.html')

@app.route('/landing')
def landing():
    """Landing page - always shows landing regardless of login status"""
    return render_template('landing.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    """Login page for both students and counselors"""
    if request.method == 'POST':
        email = request.form.get('email')
        password = request.form.get('password')
        user_type = request.form.get('user_type', 'student')
        
        conn = get_db()
        cursor = conn.cursor()
        
        if user_type == 'student':
            cursor.execute('SELECT * FROM students WHERE email = ? AND password = ?', (email, password))
            user = cursor.fetchone()
            if user:
                session['user_id'] = user['id']
                session['user_name'] = user['name']
                session['user_type'] = 'student'
                conn.close()
                flash('Welcome back!', 'success')
                return redirect(url_for('student_dashboard', id=user['id']))
        else:
            cursor.execute('SELECT * FROM counselors WHERE email = ? AND password = ?', (email, password))
            user = cursor.fetchone()
            if user:
                session['user_id'] = user['id']
                session['user_name'] = user['name']
                session['user_type'] = 'counselor'
                conn.close()
                flash('Welcome back!', 'success')
                return redirect(url_for('counselor_dashboard'))
        
        conn.close()
        flash('Invalid email or password', 'error')
    
    return render_template('login.html')

@app.route('/counselor-login', methods=['GET', 'POST'])
def counselor_login():
    """Counselor login page - redirects to main login"""
    return redirect(url_for('login'))

@app.route('/register', methods=['GET', 'POST'])
def register():
    """Registration page for students"""
    if request.method == 'POST':
        name = request.form.get('name')
        email = request.form.get('email')
        password = request.form.get('password')
        roll_number = request.form.get('roll_number')
        department = request.form.get('department')
        semester = request.form.get('semester')
        
        conn = get_db()
        cursor = conn.cursor()
        
        try:
            cursor.execute('''
                INSERT INTO students (name, email, password, roll_number, department, semester)
                VALUES (?, ?, ?, ?, ?, ?)
            ''', (name, email, password, roll_number, department, semester))
            conn.commit()
            
            # Get the new student's ID
            student_id = cursor.lastrowid
            
            session['user_id'] = student_id
            session['user_name'] = name
            session['user_type'] = 'student'
            
            conn.close()
            flash('Registration successful!', 'success')
            return redirect(url_for('student_dashboard', id=student_id))
        except sqlite3.IntegrityError:
            conn.close()
            flash('Email or Roll Number already exists', 'error')
    
    return render_template('register.html')

@app.route('/counselor-register', methods=['GET', 'POST'])
def counselor_register():
    """Registration page for counselors"""
    if request.method == 'POST':
        name = request.form.get('name')
        email = request.form.get('email')
        password = request.form.get('password')
        phone = request.form.get('phone')
        employee_id = request.form.get('employee_id')
        license_number = request.form.get('license_number')
        specialization = request.form.get('specialization')
        qualifications = request.form.get('qualifications')
        experience_years = request.form.get('experience_years')
        department = request.form.get('department', '')
        
        conn = get_db()
        cursor = conn.cursor()
        
        try:
            cursor.execute('''
                INSERT INTO counselors (name, email, password, phone, employee_id, license_number, 
                                       specialization, qualifications, experience_years, department)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ''', (name, email, password, phone, employee_id, license_number, 
                  specialization, qualifications, experience_years, department))
            conn.commit()
            
            # Get the new counselor's ID
            counselor_id = cursor.lastrowid
            
            session['user_id'] = counselor_id
            session['user_name'] = name
            session['user_type'] = 'counselor'
            
            conn.close()
            flash('Registration successful! Welcome to IRA.', 'success')
            return redirect(url_for('counselor_dashboard'))
        except sqlite3.IntegrityError as e:
            conn.close()
            if 'email' in str(e).lower():
                flash('Email already exists', 'error')
            elif 'employee_id' in str(e).lower():
                flash('Employee ID already exists', 'error')
            else:
                flash('Registration failed. Please check your information.', 'error')
    
    return render_template('counselor_register.html')

@app.route('/logout')
def logout():
    """Logout user"""
    session.clear()
    flash('You have been logged out', 'info')
    return redirect(url_for('login'))

@app.route('/student/<int:id>')
def student_dashboard(id):
    """Student dashboard showing all their data and risk assessment"""
    if 'user_id' not in session or session.get('user_type') != 'student' or session['user_id'] != id:
        flash('Please login to continue', 'error')
        return redirect(url_for('login'))
    
    conn = get_db()
    cursor = conn.cursor()
    
    # Get student info
    cursor.execute('SELECT * FROM students WHERE id = ?', (id,))
    student = cursor.fetchone()
    
    # Calculate risk
    risk_level, risk_score, factors = calculate_risk_score(id)
    
    # Get wellness tips
    tips = get_wellness_tips(risk_level, factors)
    
    # Get recent moods (last 7 days)
    cursor.execute('''
        SELECT * FROM moods 
        WHERE student_id = ? 
        ORDER BY created_at DESC 
        LIMIT 7
    ''', (id,))
    moods = cursor.fetchall()
    
    # Get recent activities
    cursor.execute('''
        SELECT * FROM activities 
        WHERE student_id = ? 
        ORDER BY date DESC 
        LIMIT 7
    ''', (id,))
    activities = cursor.fetchall()
    
    # Get attendance records
    cursor.execute('''
        SELECT * FROM attendance 
        WHERE student_id = ? 
        ORDER BY id DESC
    ''', (id,))
    attendance = cursor.fetchall()
    
    # Get recent journals
    cursor.execute('''
        SELECT * FROM journals 
        WHERE student_id = ? 
        ORDER BY created_at DESC 
        LIMIT 5
    ''', (id,))
    journals = cursor.fetchall()
    
    conn.close()
    
    return render_template('student_dashboard.html', 
                         student=student,
                         risk_level=risk_level,
                         risk_score=risk_score,
                         factors=factors,
                         tips=tips,
                         moods=moods,
                         activities=activities,
                         attendance=attendance,
                         journals=journals)

@app.route('/mood', methods=['GET', 'POST'])
def mood():
    """Mood input page"""
    if 'user_id' not in session or session.get('user_type') != 'student':
        flash('Please login to continue', 'error')
        return redirect(url_for('login'))
    
    if request.method == 'POST':
        mood_score = request.form.get('mood_score')
        notes = request.form.get('notes', '')
        
        conn = get_db()
        cursor = conn.cursor()
        
        cursor.execute('''
            INSERT INTO moods (student_id, mood_score, notes)
            VALUES (?, ?, ?)
        ''', (session['user_id'], mood_score, notes))
        
        conn.commit()
        conn.close()
        
        flash('Mood recorded successfully!', 'success')
        return redirect(url_for('student_dashboard', id=session['user_id']))
    
    return render_template('mood.html')

@app.route('/journal', methods=['GET', 'POST'])
def journal():
    """Journal page for students"""
    if 'user_id' not in session or session.get('user_type') != 'student':
        flash('Please login to continue', 'error')
        return redirect(url_for('login'))
    
    if request.method == 'POST':
        title = request.form.get('title')
        content = request.form.get('content')
        
        conn = get_db()
        cursor = conn.cursor()
        
        cursor.execute('''
            INSERT INTO journals (student_id, title, content)
            VALUES (?, ?, ?)
        ''', (session['user_id'], title, content))
        
        conn.commit()
        conn.close()
        
        flash('Journal entry saved!', 'success')
        return redirect(url_for('journal'))
    
    # Get existing journals
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('''
        SELECT * FROM journals 
        WHERE student_id = ? 
        ORDER BY created_at DESC
    ''', (session['user_id'],))
    journals = cursor.fetchall()
    conn.close()
    
    return render_template('journal.html', journals=journals)

@app.route('/schedule_meeting', methods=['POST'])
def schedule_meeting():
    """Schedule a meeting with counselor"""
    if 'user_id' not in session or session.get('user_type') != 'student':
        return jsonify({'success': False, 'message': 'Unauthorized'}), 401
    
    conn = get_db()
    cursor = conn.cursor()
    
    # Insert meeting request
    cursor.execute('''
        INSERT INTO meetings (student_id, status)
        VALUES (?, 'scheduled')
    ''', (session['user_id'],))
    
    # Get student info
    cursor.execute('SELECT name, roll_number FROM students WHERE id = ?', (session['user_id'],))
    student = cursor.fetchone()
    
    # Create notification for all counselors
    cursor.execute('SELECT id FROM counselors')
    counselors = cursor.fetchall()
    
    for counselor in counselors:
        cursor.execute('''
            INSERT INTO notifications (user_id, user_type, title, message, link, is_read, reference_id)
            VALUES (?, 'counselor', ?, ?, ?, 0, ?)
        ''', (
            counselor['id'],
            'New Meeting Request',
            f"{student['name']} ({student['roll_number']}) has requested a counseling session.",
            f"/counselor#student{session['user_id']}",
            session['user_id']
        ))
    
    conn.commit()
    conn.close()
    
    return jsonify({'success': True, 'message': 'Meeting scheduled successfully! You will receive a call soon.'})

@app.route('/schedule_meeting_for_student/<int:student_id>', methods=['POST'])
def schedule_meeting_for_student(student_id):
    """Counselor schedules a meeting with a student"""
    if 'user_id' not in session or session.get('user_type') != 'counselor':
        return jsonify({'success': False, 'message': 'Unauthorized'}), 401
    
    conn = get_db()
    cursor = conn.cursor()
    
    # Get student info
    cursor.execute('SELECT name, roll_number FROM students WHERE id = ?', (student_id,))
    student = cursor.fetchone()
    
    if not student:
        return jsonify({'success': False, 'message': 'Student not found'}), 404
    
    # Insert meeting
    cursor.execute('''
        INSERT INTO meetings (student_id, counselor_id, status)
        VALUES (?, ?, 'scheduled')
    ''', (student_id, session['user_id']))
    
    # Create notification for student
    cursor.execute('''
        INSERT INTO notifications (user_id, user_type, title, message, link, is_read, reference_id)
        VALUES (?, 'student', ?, ?, ?, 0, ?)
    ''', (
        student_id,
        'Counseling Session Scheduled',
        'A counselor has scheduled a session with you. You will be contacted soon.',
        f"/student/{student_id}",
        student_id
    ))
    
    conn.commit()
    conn.close()
    
    return jsonify({
        'success': True, 
        'message': f'Meeting scheduled with {student["name"]}. Student has been notified.'
    })

@app.route('/notifications')
def get_notifications():
    """Get notifications for current user"""
    if 'user_id' not in session:
        return jsonify({'success': False, 'message': 'Unauthorized'}), 401
    
    conn = get_db()
    cursor = conn.cursor()
    
    cursor.execute('''
        SELECT * FROM notifications 
        WHERE user_id = ? AND user_type = ?
        ORDER BY created_at DESC
        LIMIT 20
    ''', (session['user_id'], session.get('user_type')))
    
    notifications = [dict(row) for row in cursor.fetchall()]
    
    # Get unread count
    cursor.execute('''
        SELECT COUNT(*) as count FROM notifications 
        WHERE user_id = ? AND user_type = ? AND is_read = 0
    ''', (session['user_id'], session.get('user_type')))
    
    unread_count = cursor.fetchone()['count']
    
    conn.close()
    
    return jsonify({
        'success': True,
        'notifications': notifications,
        'unread_count': unread_count
    })

@app.route('/mark_notification_read/<int:notification_id>', methods=['POST'])
def mark_notification_read(notification_id):
    """Mark a notification as read"""
    if 'user_id' not in session:
        return jsonify({'success': False, 'message': 'Unauthorized'}), 401
    
    conn = get_db()
    cursor = conn.cursor()
    
    cursor.execute('''
        UPDATE notifications 
        SET is_read = 1 
        WHERE id = ? AND user_id = ? AND user_type = ?
    ''', (notification_id, session['user_id'], session.get('user_type')))
    
    conn.commit()
    conn.close()
    
    return jsonify({'success': True})

@app.route('/counselor')
def counselor_dashboard():
    """Counselor dashboard showing all students sorted by risk"""
    if 'user_id' not in session or session.get('user_type') != 'counselor':
        flash('Please login as counselor to continue', 'error')
        return redirect(url_for('login'))
    
    conn = get_db()
    cursor = conn.cursor()
    
    # Get all students
    cursor.execute('SELECT * FROM students ORDER BY name')
    students = cursor.fetchall()
    
    # Calculate risk for each student
    students_with_risk = []
    risk_counts = {'high': 0, 'moderate': 0, 'low': 0}
    
    for student in students:
        risk_level, risk_score, factors = calculate_risk_score(student['id'])
        students_with_risk.append({
            'id': student['id'],
            'name': student['name'],
            'email': student['email'],
            'roll_number': student['roll_number'],
            'department': student['department'],
            'semester': student['semester'],
            'cgpa': student['cgpa'],
            'risk_level': risk_level,
            'risk_score': risk_score,
            'factors': factors
        })
        risk_counts[risk_level] += 1
    
    # Sort by risk score (highest first)
    students_with_risk.sort(key=lambda x: x['risk_score'], reverse=True)
    
    # Get upcoming meetings
    cursor.execute('''
        SELECT m.*, s.name as student_name, s.roll_number
        FROM meetings m
        JOIN students s ON m.student_id = s.id
        WHERE m.status = 'scheduled'
        ORDER BY m.scheduled_at DESC
    ''')
    meetings = cursor.fetchall()
    
    conn.close()
    
    return render_template('counselor_dashboard.html',
                         students=students_with_risk,
                         risk_counts=risk_counts,
                         meetings=meetings)

@app.route('/student_details/<int:id>')
def student_details(id):
    """Get detailed student data for counselor view (AJAX)"""
    if 'user_id' not in session or session.get('user_type') != 'counselor':
        return jsonify({'error': 'Unauthorized'}), 401
    
    conn = get_db()
    cursor = conn.cursor()
    
    # Get moods for past 7 days
    cursor.execute('''
        SELECT DATE(created_at) as date, AVG(mood_score) as avg_mood
        FROM moods 
        WHERE student_id = ?
        AND created_at >= datetime('now', '-7 days')
        GROUP BY DATE(created_at)
        ORDER BY date
    ''', (id,))
    moods = [dict(row) for row in cursor.fetchall()]
    
    # Get activities for past 7 days
    cursor.execute('''
        SELECT * FROM activities 
        WHERE student_id = ? 
        ORDER BY date DESC 
        LIMIT 7
    ''', (id,))
    activities = [dict(row) for row in cursor.fetchall()]
    
    # Get attendance
    cursor.execute('''
        SELECT * FROM attendance 
        WHERE student_id = ? 
        ORDER BY id DESC
        LIMIT 4
    ''', (id,))
    attendance = [dict(row) for row in cursor.fetchall()]
    
    conn.close()
    
    return jsonify({
        'moods': moods,
        'activities': activities,
        'attendance': attendance
    })

@app.route('/analyze_mood', methods=['POST'])
def analyze_mood():
    """
    Analyze emotion/mood from journal text using AI model
    Accepts: { "text": "journal entry..." }
    Returns: { "emotion": "sadness", "score": 0.87, "all_emotions": [...] }
    """
    global emotion_analyzer, ai_models_loading
    
    if ai_models_loading:
        return jsonify({
            'success': False,
            'error': 'AI models are still loading. Please try again in a moment.'
        }), 503
    
    if not emotion_analyzer:
        return jsonify({
            'success': False,
            'error': 'Emotion analyzer not initialized'
        }), 503
    
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing "text" field in request body'
            }), 400
        
        text = data['text']
        
        if not text or not text.strip():
            return jsonify({
                'success': False,
                'error': 'Text cannot be empty'
            }), 400
        
        # Analyze emotion
        result = emotion_analyzer.analyze(text)
        
        return jsonify({
            'success': True,
            'emotion': result['emotion'],
            'score': result['score'],
            'all_emotions': result.get('all_emotions', [])
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/predict_dropout', methods=['POST'])
def predict_dropout():
    """
    Predict dropout risk using tabular classification model
    Accepts: JSON with student features (numeric + mood/emotion features)
    Returns: { "risk_score": 0.78, "risk_category": "high", "explanation": [...] }
    """
    global dropout_predictor, emotion_analyzer, ai_models_loading
    
    if ai_models_loading:
        return jsonify({
            'success': False,
            'error': 'AI models are still loading. Please try again in a moment.'
        }), 503
    
    if not dropout_predictor:
        return jsonify({
            'success': False,
            'error': 'Dropout predictor not initialized'
        }), 503
    
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({
                'success': False,
                'error': 'Missing request body'
            }), 400
        
        # Extract student data
        student_data = {
            'cgpa': data.get('cgpa', 7.0),
            'attendance_percentage': data.get('attendance_percentage', 85.0),
            'fee_pending': data.get('fee_pending', False),
            'mood_score': data.get('mood_score', 6.5),
            'activities_per_week': data.get('activities_per_week', 3.0),
            'semester': data.get('semester', 4)
        }
        
        # Handle emotion data
        emotion_data = None
        
        # Option 1: Emotion data provided directly
        if 'emotion_data' in data:
            emotion_data = data['emotion_data']
        
        # Option 2: Text provided for emotion analysis
        elif 'text' in data and emotion_analyzer:
            text = data['text']
            if text and text.strip():
                emotion_data = emotion_analyzer.analyze(text)
        
        # Option 3: Individual emotion scores provided
        elif any(key in data for key in ['emotion_joy', 'emotion_sadness', 'emotion_anger', 'emotion_fear']):
            emotion_data = {
                'all_emotions': [
                    {'emotion': 'joy', 'score': data.get('emotion_joy', 0.0)},
                    {'emotion': 'sadness', 'score': data.get('emotion_sadness', 0.0)},
                    {'emotion': 'anger', 'score': data.get('emotion_anger', 0.0)},
                    {'emotion': 'fear', 'score': data.get('emotion_fear', 0.0)}
                ]
            }
        
        # Make prediction
        result = dropout_predictor.predict(student_data, emotion_data)
        
        return jsonify({
            'success': True,
            'risk_score': result['risk_score'],
            'risk_category': result['risk_category'],
            'explanation': result['explanation'],
            'risk_probabilities': result.get('risk_probabilities', {})
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/chat', methods=['POST'])
def chat():
    """
    Gemini AI Chatbot endpoint - streams responses in real-time
    Accepts: { "message": "user message" }
    Returns: Server-Sent Events stream
    """
    if 'user_id' not in session or session.get('user_type') != 'student':
        return jsonify({
            'success': False,
            'error': 'Unauthorized'
        }), 401
    
    try:
        data = request.get_json()
        
        if not data or 'message' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing "message" in request body'
            }), 400
        
        user_message = data['message']
        
        if not user_message.strip():
            return jsonify({
                'success': False,
                'error': 'Message cannot be empty'
            }), 400
        
        # Check if Gemini is configured
        if not GEMINI_API_KEY or GEMINI_API_KEY == 'your_gemini_api_key_here':
            # Fallback response
            return jsonify({
                'success': True,
                'response': "Hi! I'm Ira, your wellbeing companion. 🧸 I'm here to support you! (Note: Gemini API key not configured - using fallback mode)",
                'model': 'fallback'
            })
        
        def generate():
            try:
                # Initialize Gemini model
                model = genai.GenerativeModel('gemini-2.0-flash')
                
                # Create chat with system prompt
                chat = model.start_chat(history=[
                    {
                        'role': 'user',
                        'parts': [SYSTEM_PROMPT]
                    },
                    {
                        'role': 'model',
                        'parts': ["I understand. I'm Ira.ai, here to support students with empathy and warmth. I'll keep my responses natural, concise, and helpful. How can I help you today?"]
                    }
                ])
                
                # Stream response
                response = chat.send_message(user_message, stream=True)
                
                for chunk in response:
                    if chunk.text:
                        # Send chunk as JSON
                        yield f"data: {json.dumps({'chunk': chunk.text})}\n\n"
                
                # Send completion signal
                yield f"data: {json.dumps({'done': True})}\n\n"
                
            except Exception as e:
                # Log the actual error for debugging
                print(f"❌ Gemini Error: {str(e)}")
                import traceback
                traceback.print_exc()
                
                error_msg = f"I apologize, but I encountered an error. Please try again. 🧸"
                yield f"data: {json.dumps({'chunk': error_msg, 'error': True})}\n\n"
                yield f"data: {json.dumps({'done': True})}\n\n"
        
        return Response(
            stream_with_context(generate()),
            mimetype='text/event-stream',
            headers={
                'Cache-Control': 'no-cache',
                'X-Accel-Buffering': 'no'
            }
        )
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/health')
def health():
    """Health check endpoint for deployment platforms"""
    return jsonify({
        'status': 'healthy',
        'ai_models_loaded': not ai_models_loading,
        'database': 'connected'
    }), 200

if __name__ == '__main__':
    # Use /tmp on Render for database if available
    db_path = '/tmp/ira.db' if os.getenv('RENDER') else 'instance/ira.db'
    app.config['DATABASE'] = db_path

    # Ensure the directory exists
    os.makedirs(os.path.dirname(db_path), exist_ok=True)

    # Create the database if missing
    if not os.path.exists(db_path):
        print(f"Creating new database at {db_path}")
        try:
            from create_database import create_database
            create_database(db_path)
            print("✅ Database created successfully with sample data!")
        except Exception as e:
            print(f"Warning: Could not run full database creation: {e}")
            print("Initializing with basic schema only...")
            init_db()
        else:
            print("Database initialized successfully.")
    else:
        print(f"✅ Database found at {db_path}")

    # Initialize AI models in background (non-blocking)
    initialize_ai_models_background()
    
    print("Starting IRA - Intuitive Reflection and Alert")
    print("App starting immediately, AI models loading in background...")

    # Run Flask (Render provides PORT)
    port = int(os.environ.get('PORT', 10000))
    app.run(host='0.0.0.0', port=port, debug=False)

