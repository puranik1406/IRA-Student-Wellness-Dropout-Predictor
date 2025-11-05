# 🚀 Quick Start Guide - IRA

Get IRA up and running in **5 minutes**!

---

## ✅ Prerequisites

- Python 3.8+ installed
- pip (comes with Python)
- ~2GB free disk space
- Internet connection (first-time only)

---

## 📦 Step 1: Install Dependencies

```bash
# Navigate to IRA directory
cd IRA

# Install Python packages
pip install -r requirements.txt
```

**Note**: First install downloads AI models (~500MB-1GB). This is automatic.

---

## 💾 Step 2: Create Database

```bash
python create_database.py
```

**✅ Expected Output:**

```
✅ Database created successfully!
✅ Sample data inserted!

📝 Login Credentials:
Student: aarav@student.edu / student123
Counselor: counselor@ira.edu / counselor123
```

---

## 🚀 Step 3: Run the Application

```bash
python app.py
```

**✅ Expected Output:**

```
🤖 Initializing AI models...
✅ Emotion analyzer loaded successfully
✅ Dropout risk predictor loaded successfully
🎉 All AI models initialized successfully!
🚀 Starting IRA - Intuitive Reflection and Alert
📍 Access the application at: http://127.0.0.1:5000
```

---

## 🌐 Step 4: Access the Application

Open your browser and go to:

```
http://127.0.0.1:5000
```

---

## 🎓 Using IRA as a Student

### Login

- Email: `aarav@student.edu`
- Password: `student123`

### Features Available

1. **Dashboard Overview**
    - View your risk level (High/Moderate/Low)
    - Check CGPA and attendance stats
    - See AI wellness insights carousel

2. **Log Your Mood** 😊
    - Click "Log Mood" button
    - Rate your mood (1-10)
    - Add optional notes

3. **Write Journal** 📝
    - Click "Write Journal" link
    - Express your thoughts
    - AI analyzes emotions from your entries

4. **Schedule Counselor Meeting** 📅
    - Click "Schedule Counselor Meeting" button
    - Instant confirmation
    - Counselor will be notified

5. **Help & Resources** ❓
    - Click help icon (bottom-right)
    - Access emergency contacts
    - View mental health resources

---

## 👨‍🏫 Using IRA as a Counselor

### Login

- Email: `counselor@ira.edu`
- Password: `counselor123`

### Features Available

1. **Risk Dashboard**
    - See all students sorted by risk level
    - View risk counters (High/Moderate/Low)

2. **Student Details**
    - Click on any student card
    - View detailed information:
        - Mood trends
        - Attendance records
        - Activity data
        - Risk factors

3. **Schedule Meetings**
    - Click "Schedule Meeting" on student card
    - Student gets notified automatically

4. **View Analytics**
    - Interactive charts for mood trends
    - Weekly attendance patterns
    - Risk progression over time

---

## 🛠️ Troubleshooting

### Database Not Found

```bash
python create_database.py
```

### Port Already in Use

Edit `app.py` and change the port:

```python
app.run(debug=True, port=5001)
```

### Module Not Found

```bash
pip install -r requirements.txt
```

### AI Models Not Loading

- Check internet connection (first-time download)
- Wait 2-3 minutes for models to load
- Check console for error messages
- Restart application

---

## 📊 Understanding Risk Levels

### Risk Calculation

IRA uses multiple factors:

- **CGPA**: <6.0 = Critical, <7.0 = Concerning
- **Attendance**: <70% = Critical, <75% = Concerning
- **Fee Status**: Pending = Risk factor
- **Mental Health**: Low mood scores = Higher risk

### Risk Categories

- 🔴 **High Risk** (≥50 points): Requires immediate attention
- 🟡 **Moderate Risk** (30-49 points): Needs monitoring
- 🟢 **Low Risk** (<30 points): Doing well

---

## 🎯 Key Features

### AI-Powered

- ✅ **Emotion Detection**: Analyzes journal entries for emotional patterns
- ✅ **Dropout Prediction**: Uses 10+ factors to predict risk
- ✅ **Explainable AI**: Detailed breakdown of risk factors
- ✅ **No API Keys**: All models run locally

### User-Friendly

- ✅ **Bento Grid Layout**: Modern, organized dashboard
- ✅ **Color-Coded Risk**: Visual indicators (red/yellow/green)
- ✅ **Interactive Charts**: Mood and activity trends
- ✅ **Responsive Design**: Works on all devices

### Privacy-First

- ✅ **Local Processing**: All AI runs on your machine
- ✅ **Secure Sessions**: Session-based authentication
- ✅ **Private Journals**: Only you can see your entries

---

## 📁 File Structure

```
IRA/
├── app.py                      # Main application
├── create_database.py          # Database setup
├── requirements.txt            # Dependencies
├── templates/                  # HTML templates
│   ├── student_dashboard.html  # Student interface
│   ├── counselor_dashboard.html # Counselor interface
│   ├── mood.html               # Mood logging
│   └── journal.html            # Journal page
├── ai_models/                  # AI models
│   ├── emotion_model.py        # Emotion detection
│   └── tabular_model.py        # Dropout prediction
└── instance/
    └── ira.db                  # Database (created on init)
```

---

## 🔧 Configuration

Create a `.env` file for custom settings:

```env
SECRET_KEY=your_secret_key_here
DEBUG=True
```

---

## 📈 Next Steps

1. **Explore Student Dashboard** - Log moods and write journals
2. **Check Counselor Dashboard** - View all students' risk levels
3. **Test AI Features** - Run `python test_ai_endpoints.py`
4. **Customize Risk Algorithm** - Edit `calculate_risk_score()` in `app.py`
5. **Add More Students** - Use the registration page

---

## 🆘 Getting Help

- Check console logs for errors
- Read `README.md` for detailed documentation
- See `ai_models/README.md` for AI model details

---

## 📝 Demo Credentials

**Students**:

- `aarav@student.edu` / `student123`
- `priya@student.edu` / `student123`
- `rohan@student.edu` / `student123`

**Counselors**:

- `counselor@ira.edu` / `counselor123`

---

**That's it! You're ready to use IRA** 🎉

**Built with ❤️ for student wellness and success**

