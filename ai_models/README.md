# AI Models Integration - IRA Student Dropout Prevention System

This directory contains AI models for emotion detection and dropout risk prediction using Hugging
Face models.

## 📁 Structure

```
ai_models/
├── __init__.py           # Package initialization
├── emotion_model.py      # Emotion/mood detection model
├── tabular_model.py      # Dropout risk prediction model
└── README.md            # This file
```

## 🤖 Models Used

### 1. Emotion Detection Model

- **Model**: `j-hartmann/emotion-english-distilroberta-base`
- **Alternative**: `michellejieli/emotion_text_classifier`
- **Type**: Text classification using DistilRoBERTa
- **Framework**: Hugging Face Transformers
- **Purpose**: Analyze emotions in journal entries and text inputs

**Detected Emotions**:

- Joy / Happiness
- Sadness
- Anger
- Fear
- Surprise
- Neutral

### 2. Tabular Classification Model

- **Primary Model**: `Prior-Labs/TabPFN-v2-clf` (TabPFN)
- **Fallback**: Random Forest Classifier (scikit-learn)
- **Purpose**: Predict dropout risk based on student metrics

**Input Features**:

1. CGPA (0-10 scale)
2. Attendance percentage (0-100)
3. Fee pending status (boolean)
4. Mood score (1-10 scale)
5. Activities per week (numeric)
6. Emotion scores (joy, sadness, anger, fear)
7. Current semester (1-8)

**Output Categories**:

- **Low Risk**: risk_score < 0.3
- **Moderate Risk**: 0.3 ≤ risk_score < 0.6
- **High Risk**: risk_score ≥ 0.6

## 🚀 API Endpoints

### 1. Analyze Mood/Emotion

**Endpoint**: `POST /analyze_mood`

**Request Body**:

```json
{
  "text": "I'm feeling really happy and excited about my classes today!"
}
```

**Response**:

```json
{
  "success": true,
  "emotion": "joy",
  "score": 0.8542,
  "all_emotions": [
    {"emotion": "joy", "score": 0.8542},
    {"emotion": "neutral", "score": 0.0892},
    {"emotion": "surprise", "score": 0.0321}
  ]
}
```

**cURL Example**:

```bash
curl -X POST http://127.0.0.1:5000/analyze_mood \
  -H "Content-Type: application/json" \
  -d '{"text": "I am so stressed about my exams"}'
```

**Python Example**:

```python
import requests

response = requests.post(
    "http://127.0.0.1:5000/analyze_mood",
    json={"text": "I'm feeling great today!"}
)

result = response.json()
print(f"Emotion: {result['emotion']}, Score: {result['score']}")
```

### 2. Predict Dropout Risk

**Endpoint**: `POST /predict_dropout`

**Request Body Options**:

**Option A - With Emotion Scores**:

```json
{
  "cgpa": 7.5,
  "attendance_percentage": 85.0,
  "fee_pending": false,
  "mood_score": 6.5,
  "activities_per_week": 3.0,
  "semester": 4,
  "emotion_joy": 0.6,
  "emotion_sadness": 0.2,
  "emotion_anger": 0.1,
  "emotion_fear": 0.1
}
```

**Option B - With Text (Auto-analyzes emotion)**:

```json
{
  "cgpa": 5.5,
  "attendance_percentage": 65.0,
  "fee_pending": true,
  "mood_score": 3.0,
  "activities_per_week": 1.0,
  "semester": 5,
  "text": "I'm feeling overwhelmed and stressed about everything."
}
```

**Response**:

```json
{
  "success": true,
  "risk_score": 0.7843,
  "risk_category": "high",
  "risk_probabilities": {
    "low": 0.0523,
    "moderate": 0.2154,
    "high": 0.7323
  },
  "explanation": [
    "⚠️ Critical CGPA: 5.50 - Below minimum requirement",
    "⚠️ Low attendance: 65.0% - Below required 75%",
    "⚠️ Fee payment pending - May affect enrollment",
    "⚠️ Low mood score: 3.0/10 - Mental health support recommended",
    "⚠️ Low engagement: 1.0 activities/week"
  ]
}
```

**cURL Example**:

```bash
curl -X POST http://127.0.0.1:5000/predict_dropout \
  -H "Content-Type: application/json" \
  -d '{
    "cgpa": 7.0,
    "attendance_percentage": 78.0,
    "fee_pending": false,
    "mood_score": 6.0,
    "activities_per_week": 2.5,
    "semester": 4,
    "text": "Feeling a bit stressed but managing."
  }'
```

**Python Example**:

```python
import requests

student_data = {
    "cgpa": 8.5,
    "attendance_percentage": 92.0,
    "fee_pending": False,
    "mood_score": 8.5,
    "activities_per_week": 5.0,
    "semester": 3,
    "emotion_joy": 0.8,
    "emotion_sadness": 0.1,
    "emotion_anger": 0.05,
    "emotion_fear": 0.05
}

response = requests.post(
    "http://127.0.0.1:5000/predict_dropout",
    json=student_data
)

result = response.json()
print(f"Risk: {result['risk_category']} ({result['risk_score']:.2%})")
for factor in result['explanation']:
    print(f"  - {factor}")
```

## 🔧 Installation

### Install Dependencies

```bash
# Navigate to project root
cd ira

# Install required packages
pip install -r requirements.txt
```

### Required Packages

```txt
transformers==4.36.0
torch==2.1.0
scikit-learn==1.3.2
numpy==1.24.3
pandas==2.1.3
sentencepiece==0.1.99
tabpfn==0.1.10
```

**Note**: If `tabpfn` installation fails, the system will automatically fall back to a RandomForest
classifier.

## 🎯 Usage in Application

### Initialize Models at Startup

The models are automatically initialized when the Flask application starts:

```python
# In app.py
from ai_models import EmotionAnalyzer, DropoutRiskPredictor

# Initialize at startup
emotion_analyzer = EmotionAnalyzer()
dropout_predictor = DropoutRiskPredictor()
```

### Use in Code

```python
# Analyze emotion
from ai_models import EmotionAnalyzer

analyzer = EmotionAnalyzer()
result = analyzer.analyze("I'm feeling great today!")
print(result)
# Output: {'emotion': 'joy', 'score': 0.8542, 'all_emotions': [...]}

# Predict dropout risk
from ai_models import DropoutRiskPredictor

predictor = DropoutRiskPredictor()
student_data = {
    'cgpa': 7.5,
    'attendance_percentage': 85.0,
    'fee_pending': False,
    'mood_score': 7.0,
    'activities_per_week': 3.0,
    'semester': 4
}

result = predictor.predict(student_data)
print(result)
# Output: {'risk_score': 0.25, 'risk_category': 'low', 'explanation': [...]}
```

## 🧪 Testing

Run the test suite to verify the AI endpoints:

```bash
# Make sure Flask app is running
python app.py

# In another terminal, run tests
python test_ai_endpoints.py
```

Expected output:

```
============================================================
AI Model Endpoints Test Suite
============================================================

✅ Server is running!

============================================================
Testing /analyze_mood endpoint
============================================================

--- Test 1 ---
Text: I'm feeling really happy and excited about my classes...
✅ Success!
   Emotion: joy
   Score: 0.8542
   ...
```

## 🔑 Environment Variables (Optional)

For private/gated Hugging Face models, set:

```bash
# .env file
HUGGINGFACEHUB_API_TOKEN=your_token_here
```

Or in PowerShell:

```powershell
$env:HUGGINGFACEHUB_API_TOKEN="your_token_here"
```

**Note**: The current models (`j-hartmann/emotion-english-distilroberta-base` and TabPFN) are public
and don't require authentication.

## 📊 Model Performance

### Emotion Model

- **Accuracy**: ~94% on WASSA-2017 dataset
- **Languages**: English only
- **Input Limit**: 512 tokens (~400 words)
- **Processing Time**: ~100-500ms per request

### Dropout Predictor

- **Training Data**: Synthetic data (200 samples)
- **Features**: 10 input features
- **Classes**: 3 (low, moderate, high risk)
- **Fallback**: RandomForest with 100 estimators

**Note**: For production use, the dropout predictor should be retrained with real student data from
your institution.

## 🛠️ Troubleshooting

### Model Loading Errors

If you see "Emotion analyzer not initialized":

1. Check that `transformers` and `torch` are installed
2. Ensure you have internet connection for first-time model download
3. Check logs for specific error messages

### TabPFN Installation Issues

If TabPFN fails to install:

- The system automatically falls back to RandomForest
- This is expected on some systems and doesn't affect functionality
- Performance is still good with the fallback model

### Memory Issues

For systems with limited RAM:

- Models use ~500MB-1GB of memory
- Consider using CPU instead of GPU
- Reduce `N_ensemble_configurations` in TabPFN

## 📝 Future Improvements

1. **Custom Training**: Train models on real institutional data
2. **Multi-language**: Add support for regional languages
3. **Ensemble Models**: Combine multiple emotion models
4. **Real-time Updates**: Continuously update models with new data
5. **Explainability**: Add SHAP/LIME for better interpretability

## 📚 References

- [j-hartmann emotion model](https://huggingface.co/j-hartmann/emotion-english-distilroberta-base)
- [TabPFN paper](https://arxiv.org/abs/2207.01848)
- [Hugging Face Transformers](https://huggingface.co/docs/transformers)
- [scikit-learn Documentation](https://scikit-learn.org/)

## 📞 Support

For issues or questions:

1. Check the logs: Models print initialization status
2. Run test suite: `python test_ai_endpoints.py`
3. Review model files: `emotion_model.py` and `tabular_model.py`

---

**Last Updated**: November 2024  
**IRA Version**: 1.0  
**AI Integration**: Complete ✅

