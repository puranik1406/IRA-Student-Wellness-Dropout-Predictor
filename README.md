# 🔆 IRA (Intuitive Reflection and Alert) - Student Dropout Prevention System

A comprehensive Flask web application that predicts and prevents college/university dropouts using
AI-powered analysis of academics, attendance, fee payments, and mental health patterns.

![Python](https://img.shields.io/badge/Python-3.8+-blue.svg)
![Flask](https://img.shields.io/badge/Flask-3.0.0-green.svg)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3.2-purple.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 👥 Founders

**IRA (Intuitive Reflection and Alert)** was created by a passionate team of students dedicated to
improving educational outcomes:

- **Ishita Puranik** - [ishitapurk14@gmail.com](mailto:ishitapurk14@gmail.com)
- **Spoorthi Chava** - [spoorthichava06@gmail.com](mailto:spoorthichava06@gmail.com)
- **Mahek Muskaan Shaik** - [mahekm.shaik@gmail.com](mailto:mahekm.shaik@gmail.com)
- **Geethanjali Bathini** - [geethanjalibathini7@gmail.com](mailto:geethanjalibathini7@gmail.com)

## ✨ Features

### Student Interface

- **📊 Comprehensive Dashboard**: View risk level, CGPA, attendance, mood trends, and activity data
- **😊 Mood Tracking**: Interactive mood logging with visual feedback (1-10 scale)
- **📝 Digital Journal**: Private journaling space for thoughts and feelings
- **🏃 Activity Monitoring**: View fitness data including steps, sleep hours, and exercise (mock
  data)
- **📅 Attendance Tracking**: Monthly attendance records with visual indicators
- **🤖 AI Wellness Insights**: Personalized tips delivered via carousel based on risk factors
- **💬 AI Chatbot (Gemini)**: 24/7 voice-enabled wellbeing companion with real-time streaming
  responses
- **📞 Counselor Meeting**: One-click scheduling with instant snackbar confirmation
- **🔔 Notifications**: Real-time notifications when counselors schedule meetings

### Counselor Interface

- **📈 Risk Dashboard**: Real-time overview of all students sorted by dropout risk
- **🚦 Color-Coded Risk Levels**:
    - 🔴 High Risk (requires immediate attention)
    - 🟡 Moderate Risk (needs monitoring)
    - 🟢 Low Risk (doing well)
- **📋 Accordion Student List**: Expandable cards with detailed student information
- **📊 Weekly Trends**: Interactive charts showing mood, sleep, and attendance patterns
- **📅 Upcoming Sessions**: Track scheduled counselor meetings
- **📊 Risk Counters**: Quick stats on total students by risk category
- **🔔 Notifications**: Instant alerts when students request meetings

### 🤖 AI-Powered Features

#### 1. Emotion Detection Model

- **Model
  **: [j-hartmann/emotion-english-distilroberta-base](https://huggingface.co/j-hartmann/emotion-english-distilroberta-base)
- **Detects**: Joy, Sadness, Anger, Fear, Surprise, Neutral
- **Purpose**: Analyze journal entries and text inputs to understand student emotional state
- **Endpoint**: `POST /analyze_mood`

**Example Usage:**

```python
# Analyze emotion from journal text
POST /analyze_mood
{
  "text": "I'm feeling really stressed about my exams..."
}

# Response
{
  "emotion": "fear",
  "score": 0.78,
  "all_emotions": [...]
}
```

#### 2. Dropout Risk Prediction Model

- **Primary Model**: TabPFN-v2 (Prior-Labs)
- **Fallback**: Random Forest Classifier
- **Input Features**: CGPA, attendance, fees, mood, activities, emotions, semester
- **Output**: Risk score (0-1), category (low/moderate/high), detailed explanation
- **Endpoint**: `POST /predict_dropout`

**Example Usage:**

```python
# Predict dropout risk
POST /predict_dropout
{
  "cgpa": 6.5,
  "attendance_percentage": 72.0,
  "fee_pending": false,
  "mood_score": 5.0,
  "activities_per_week": 2.0,
  "semester": 4,
  "text": "Feeling overwhelmed lately..."
}

# Response
{
  "risk_score": 0.65,
  "risk_category": "high",
  "explanation": [
    "⚠️ Low CGPA: 6.50 - Needs improvement",
    "⚠️ Low attendance: 72.0% - Below required 75%",
    ...
  ]
}
```

#### 3. AI Chatbot (Gemini)

- **Model**: Google Gemini 2.0 Flash
- **Purpose**: 24/7 empathetic wellbeing companion for students
- **Features**:
    - 🎤 **Voice Input** - Speak to the chatbot using Web Speech API
    - 🔊 **Voice Output** - Responses are read aloud automatically
    - 💬 **Real-time Streaming** - See responses appear word-by-word
    - 🧸 **Empathetic Tone** - Warm, supportive, and student-focused responses
    - 🎯 **Context-Aware** - Understands student wellness needs
- **Endpoint**: `POST /chat`

**Setup Instructions:**

1. Get a free Gemini API key from [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Add it to your `.env` file:
   ```env
   GEMINI_API_KEY=your_api_key_here
   ```
3. Restart the application - the chatbot will automatically activate

**Example Interaction:**

- **Student**: "I'm feeling stressed about my exams"
- **Ira.ai**: "I understand exam stress can be overwhelming. Here are some tips that might help:
  Take regular breaks (25-minute study intervals), practice deep breathing exercises, and make sure
  you're getting enough sleep. Would you like to schedule a meeting with a counselor to talk more
  about this? 🧸"

**Voice Features:**

- Click 🎤 to speak instead of typing
- Responses are automatically read aloud
- Click 🔇 to mute voice output
- Works in Chrome, Edge, and Safari

**Note**: The chatbot works in fallback mode without an API key (limited responses). For full
functionality, add your Gemini API key.

**Key Benefits:**

- 🧠 **Real-time emotion analysis** from journal entries
- 📊 **Multi-factor dropout prediction** using 10+ features
- 💡 **Explainable AI** with detailed risk factor breakdown
- 🔄 **Automatic model initialization** on application startup
- 🎯 **No API keys required** - uses open-source models

For detailed AI documentation, see [ai_models/README.md](ai_models/README.md)

### Risk Assessment Algorithm

The system uses a multi-factor heuristic approach:

- **CGPA Analysis** (30 points): Critical if <6.0, concerning if <7.0
- **Attendance** (30 points): Critical if <70%, concerning if <75%
- **Fee Status** (20 points): Pending fees add risk
- **Mental Health** (20 points): Low mood scores indicate higher risk

**Risk Levels:**

- High: Score ≥ 50
- Moderate: Score 30-49
- Low: Score < 30

## 🚀 Quick Start

### Prerequisites

- Python 3.8 or higher
- pip (Python package manager)
- Internet connection (for first-time AI model download)

### Installation

1. **Navigate to the IRA directory:**
   ```bash
   cd IRA
   ```

2. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

   **Note**: The first run will download AI models (~500MB-1GB). This happens automatically.
   If TabPFN installation fails, the system falls back to Random Forest (no functionality loss).

3. **Initialize the database:**
   ```bash
   python create_database.py
   ```

   This will create the SQLite database with sample data and display login credentials.

4. **Run the application:**
   ```bash
   python app.py
   ```

   Wait for "✅ All AI models initialized successfully!" before accessing the app.

5. **Access the application:**
   Open your browser and go to: `http://127.0.0.1:5000`

### Testing AI Endpoints

To test the AI features:

```bash
# In a separate terminal (while app.py is running)
python test_ai_endpoints.py
```

This will run comprehensive tests on both emotion detection and dropout prediction endpoints.

## 🔐 Demo Credentials

### Student Login

- **Email**: `aarav@student.edu`
- **Password**: `student123`

Other sample students: `priya@student.edu`, `rohan@student.edu`, etc. (all use password:
`student123`)

### Counselor Login

- **Email**: `counselor@ira.edu`
- **Password**: `counselor123`

## 📁 Project Structure

```
IRA/
│
├── app.py                 # Main Flask application with routes and logic
├── create_database.py     # Database initialization script
├── test_ai_endpoints.py   # AI endpoint testing suite
├── requirements.txt       # Python dependencies
├── .env                   # Environment variables (not in git)
├── .gitignore            # Git ignore rules
├── README.md             # This file
│
├── ai_models/            # AI/ML models directory
│   ├── __init__.py       # Package initialization
│   ├── emotion_model.py  # Emotion detection (Hugging Face)
│   ├── tabular_model.py  # Dropout risk prediction (TabPFN/RF)
│   └── README.md         # AI models documentation
│
├── templates/            # HTML templates
│   ├── base.html         # Base template with navbar
│   ├── login.html        # Login page
│   ├── register.html     # Student registration
│   ├── student_dashboard.html  # Student main dashboard
│   ├── mood.html         # Mood logging page
│   ├── journal.html      # Journal page
│   └── counselor_dashboard.html  # Counselor interface
│
├── static/               # Static assets
│   ├── css/
│   │   └── style.css     # Custom styles
│   └── js/
│       ├── main.js       # Main JavaScript utilities
│       └── dashboard.js  # Counselor dashboard scripts
│
├── uploads/              # File upload directory
└── instance/
    └── ira.db         # SQLite database (created on init)
```

## 🎨 UI/UX Features

### Design Elements

- **Bento Grids**: Modern card-based layouts for organized information display
- **Carousels**: Smooth sliding wellness tips with auto-play
- **Accordions**: Expandable student details in counselor dashboard
- **Color-Coded Risk**: Visual indicators (red/yellow/green) for quick assessment
- **Responsive Design**: Mobile-friendly Bootstrap 5 layout
- **Interactive Charts**: Chart.js integration for mood trends
- **Snackbar Notifications**: Non-intrusive toast messages
- **Progress Bars**: Visual representation of risk scores and attendance

### User Experience

- **Auto-dismiss Alerts**: Flash messages disappear after 5 seconds
- **Smooth Animations**: CSS transitions and transforms
- **Loading Indicators**: Spinners for async data loading
- **Sticky Navigation**: Always-accessible menu bar
- **Custom Scrollbar**: Enhanced scrolling experience

## 🧠 Technology Stack

**Backend:**

- Flask 3.0.0 - Web framework
- SQLite - Database
- Python-dotenv - Environment management
- Hugging Face Transformers - AI/ML libraries

**Frontend:**

- Bootstrap 5.3.2 - CSS framework
- Bootstrap Icons - Icon library
- Chart.js 4.4.0 - Data visualization
- Vanilla JavaScript - Interactivity

**AI/ML:**

- Transformers 4.36.0 - Hugging Face transformer models
- PyTorch 2.1.0 - Deep learning framework
- scikit-learn 1.3.2 - Machine learning utilities
- TabPFN 0.1.10 - Tabular classification (optional)
- NumPy & Pandas - Data processing

**Security:**

- Session-based authentication
- SQL injection protection via parameterized queries
- CSRF protection (Flask built-in)

## 📊 Database Schema

### Tables

- **students**: Student information and academic data
- **counselors**: Counselor accounts
- **moods**: Daily mood check-ins (1-10 scale)
- **journals**: Private journal entries
- **activities**: Fitness data (steps, sleep, exercise)
- **attendance**: Monthly attendance records
- **meetings**: Scheduled counselor sessions

## 🔧 Configuration

Edit `.env` file to customize:

```env
SECRET_KEY=your_secret_key_here
DATABASE_URI=sqlite:///instance/ira.db
DEBUG=True
GEMINI_API_KEY=your_gemini_api_key_here  # For AI chatbot (optional)
```

**Environment Variables:**

- `SECRET_KEY`: Flask session secret (change in production)
- `DATABASE_URI`: Database connection string
- `DEBUG`: Enable/disable debug mode
- `GEMINI_API_KEY`: Google Gemini API key for chatbot (get free key
  at [Google AI Studio](https://makersuite.google.com/app/apikey))

## 🚀 Deployment

For production deployment:

1. **Set DEBUG to False** in `.env`
2. **Change SECRET_KEY** to a secure random string
3. **Use a production WSGI server** like Gunicorn:
   ```bash
   pip install gunicorn
   gunicorn app:app
   ```
4. **Consider using PostgreSQL** instead of SQLite for better performance
5. **Set up HTTPS** with SSL certificates

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is licensed under the MIT License. See LICENSE for details.

## 👨‍💻 Developer Notes

### Adding New Features

- Follow Flask blueprint structure for scalability
- Use Jinja2 templates for consistency
- Add new routes in `app.py`
- Update database schema in `create_database.py`

### Customizing Risk Algorithm

Modify the `calculate_risk_score()` function in `app.py` to adjust:

- Weight of each factor (CGPA, attendance, fees, mood)
- Threshold values for risk levels
- Additional risk factors

### Styling

- Edit `static/css/style.css` for visual changes
- Use Bootstrap utility classes where possible
- Maintain responsive design principles

## 🐛 Troubleshooting

**Database not found error:**

```bash
python create_database.py
```

**Port already in use:**

```bash
# Change port in app.py
app.run(debug=True, port=5001)
```

**Module not found:**

```bash
pip install -r requirements.txt
```

## 📧 Support

For issues, questions, or suggestions, please open an issue on the project repository.

---

## 🙏 Acknowledgments

IRA was developed with passion and dedication by:

- Ishita Puranik
- Spoorthi Chava
- Mahek Muskaan Shaik
- Geethanjali Bathini

Special thanks to all the students and counselors who provided valuable feedback during development.

---

**Built with ❤️ for student wellness and success**

*IRA - Guiding students toward brighter futures* 🔆

---

**© 2025 IRA Team. Licensed under the MIT License.**