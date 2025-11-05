"""
Tabular classification model for dropout risk prediction
"""

import numpy as np
import pandas as pd
import logging
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import StandardScaler

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class DropoutRiskPredictor:
    """
    Dropout risk predictor using tabular classification
    Falls back to RandomForest if TabPFN is not available
    """
    
    def __init__(self):
        """
        Initialize the tabular model
        """
        logger.info("Initializing dropout risk predictor")
        
        self.model = None
        self.scaler = StandardScaler()
        self.use_tabpfn = False
        
        # Try to use TabPFN first
        try:
            from tabpfn import TabPFNClassifier
            logger.info("TabPFN available, using TabPFN model")
            # Note: TabPFN 2.2.1+ doesn't need N_ensemble_configurations parameter
            self.model = TabPFNClassifier(device='cpu')
            self.use_tabpfn = True
        except ImportError:
            logger.warning("TabPFN not available, falling back to RandomForest")
            self.model = RandomForestClassifier(
                n_estimators=100,
                max_depth=10,
                min_samples_split=5,
                random_state=42
            )
            self.use_tabpfn = False
        except Exception as e:
            logger.warning(f"Error loading TabPFN ({e}), falling back to RandomForest")
            self.model = RandomForestClassifier(
                n_estimators=100,
                max_depth=10,
                min_samples_split=5,
                random_state=42
            )
            self.use_tabpfn = False
        
        # Define feature names
        self.feature_names = [
            'cgpa',
            'attendance_percentage',
            'fee_pending',
            'mood_score',
            'activities_per_week',
            'emotion_joy',
            'emotion_sadness',
            'emotion_anger',
            'emotion_fear',
            'semester'
        ]
        
        # Initialize with some default training data
        self._initialize_default_model()
    
    def _initialize_default_model(self):
        """
        Initialize model with some default training data
        This allows the model to work immediately without requiring training
        """
        # Create synthetic training data
        np.random.seed(42)
        n_samples = 200
        
        # Generate features
        X_train = []
        y_train = []
        
        for _ in range(n_samples):
            # High risk students (label 2)
            if np.random.random() < 0.3:
                cgpa = np.random.uniform(4.0, 6.5)
                attendance = np.random.uniform(50, 75)
                fee_pending = np.random.choice([0, 1], p=[0.4, 0.6])
                mood_score = np.random.uniform(2, 5)
                activities = np.random.uniform(0, 2)
                emotion_joy = np.random.uniform(0, 0.3)
                emotion_sadness = np.random.uniform(0.3, 0.8)
                emotion_anger = np.random.uniform(0, 0.5)
                emotion_fear = np.random.uniform(0, 0.5)
                semester = np.random.randint(1, 9)
                y_train.append(2)  # High risk
                
            # Moderate risk students (label 1)
            elif np.random.random() < 0.6:
                cgpa = np.random.uniform(6.0, 7.5)
                attendance = np.random.uniform(70, 85)
                fee_pending = np.random.choice([0, 1], p=[0.7, 0.3])
                mood_score = np.random.uniform(4, 7)
                activities = np.random.uniform(1, 4)
                emotion_joy = np.random.uniform(0.2, 0.5)
                emotion_sadness = np.random.uniform(0.1, 0.4)
                emotion_anger = np.random.uniform(0, 0.3)
                emotion_fear = np.random.uniform(0, 0.3)
                semester = np.random.randint(1, 9)
                y_train.append(1)  # Moderate risk
                
            # Low risk students (label 0)
            else:
                cgpa = np.random.uniform(7.0, 10.0)
                attendance = np.random.uniform(80, 100)
                fee_pending = np.random.choice([0, 1], p=[0.9, 0.1])
                mood_score = np.random.uniform(6, 10)
                activities = np.random.uniform(2, 7)
                emotion_joy = np.random.uniform(0.4, 0.9)
                emotion_sadness = np.random.uniform(0, 0.2)
                emotion_anger = np.random.uniform(0, 0.2)
                emotion_fear = np.random.uniform(0, 0.2)
                semester = np.random.randint(1, 9)
                y_train.append(0)  # Low risk
            
            X_train.append([
                cgpa, attendance, fee_pending, mood_score, activities,
                emotion_joy, emotion_sadness, emotion_anger, emotion_fear, semester
            ])
        
        X_train = np.array(X_train)
        y_train = np.array(y_train)
        
        # Train the model
        try:
            if self.use_tabpfn:
                # TabPFN doesn't need scaling
                self.model.fit(X_train, y_train)
            else:
                # Scale features for RandomForest
                X_train_scaled = self.scaler.fit_transform(X_train)
                self.model.fit(X_train_scaled, y_train)
            
            logger.info("Model initialized with default training data")
        except Exception as e:
            logger.error(f"Error initializing model: {e}")
    
    def extract_features(self, student_data, emotion_data=None):
        """
        Extract features from student data and emotion analysis
        
        Args:
            student_data: dict with student information
            emotion_data: dict with emotion analysis results (optional)
            
        Returns:
            np.array: Feature vector
        """
        # Extract emotion features
        emotion_scores = {
            'joy': 0.0,
            'sadness': 0.0,
            'anger': 0.0,
            'fear': 0.0
        }
        
        if emotion_data and 'all_emotions' in emotion_data:
            for e in emotion_data['all_emotions']:
                emotion_name = e['emotion']
                if emotion_name in emotion_scores:
                    emotion_scores[emotion_name] = e['score']
        
        # Build feature vector
        features = [
            float(student_data.get('cgpa', 7.0)),
            float(student_data.get('attendance_percentage', 85.0)),
            1.0 if student_data.get('fee_pending', False) else 0.0,
            float(student_data.get('mood_score', 6.5)),
            float(student_data.get('activities_per_week', 3.0)),
            emotion_scores['joy'],
            emotion_scores['sadness'],
            emotion_scores['anger'],
            emotion_scores['fear'],
            float(student_data.get('semester', 4))
        ]
        
        return np.array(features).reshape(1, -1)
    
    def predict(self, student_data, emotion_data=None):
        """
        Predict dropout risk for a student
        
        Args:
            student_data: dict with student information
            emotion_data: dict with emotion analysis results (optional)
            
        Returns:
            dict: {
                'risk_score': float (0-1),
                'risk_category': str ('low', 'moderate', 'high'),
                'explanation': list of contributing factors
            }
        """
        try:
            # Extract features
            X = self.extract_features(student_data, emotion_data)
            
            # Make prediction
            if self.use_tabpfn:
                # TabPFN returns probabilities directly
                probabilities = self.model.predict_proba(X)[0]
            else:
                # Scale features for RandomForest
                X_scaled = self.scaler.transform(X)
                probabilities = self.model.predict_proba(X_scaled)[0]
            
            # probabilities = [P(low), P(moderate), P(high)]
            risk_score = probabilities[2] + 0.5 * probabilities[1]  # Weighted risk score
            
            # Determine risk category
            if risk_score >= 0.6:
                risk_category = 'high'
            elif risk_score >= 0.3:
                risk_category = 'moderate'
            else:
                risk_category = 'low'
            
            # Generate explanation
            explanation = self._generate_explanation(student_data, emotion_data, X[0])
            
            return {
                'risk_score': round(risk_score, 4),
                'risk_category': risk_category,
                'risk_probabilities': {
                    'low': round(float(probabilities[0]), 4),
                    'moderate': round(float(probabilities[1]), 4),
                    'high': round(float(probabilities[2]), 4)
                },
                'explanation': explanation
            }
            
        except Exception as e:
            logger.error(f"Error predicting dropout risk: {e}")
            return {
                'risk_score': 0.5,
                'risk_category': 'moderate',
                'explanation': ['Error in prediction'],
                'error': str(e)
            }
    
    def _generate_explanation(self, student_data, emotion_data, features):
        """
        Generate human-readable explanation of risk factors
        
        Args:
            student_data: Original student data
            emotion_data: Emotion analysis results
            features: Extracted feature vector
            
        Returns:
            list: List of explanation strings
        """
        explanation = []
        
        # CGPA
        cgpa = features[0]
        if cgpa < 6.0:
            explanation.append(f"⚠️ Critical CGPA: {cgpa:.2f} - Below minimum requirement")
        elif cgpa < 7.0:
            explanation.append(f"⚠️ Low CGPA: {cgpa:.2f} - Needs improvement")
        elif cgpa >= 8.5:
            explanation.append(f"✅ Excellent CGPA: {cgpa:.2f}")
        
        # Attendance
        attendance = features[1]
        if attendance < 75:
            explanation.append(f"⚠️ Low attendance: {attendance:.1f}% - Below required 75%")
        elif attendance < 85:
            explanation.append(f"⚠️ Attendance needs improvement: {attendance:.1f}%")
        else:
            explanation.append(f"✅ Good attendance: {attendance:.1f}%")
        
        # Fee pending
        if features[2] > 0.5:
            explanation.append("⚠️ Fee payment pending - May affect enrollment")
        
        # Mood
        mood = features[3]
        if mood < 4:
            explanation.append(f"⚠️ Low mood score: {mood:.1f}/10 - Mental health support recommended")
        elif mood < 6:
            explanation.append(f"⚠️ Below average mood: {mood:.1f}/10")
        elif mood >= 8:
            explanation.append(f"✅ Positive mood: {mood:.1f}/10")
        
        # Activities
        activities = features[4]
        if activities < 2:
            explanation.append(f"⚠️ Low engagement: {activities:.1f} activities/week")
        elif activities >= 4:
            explanation.append(f"✅ Good engagement: {activities:.1f} activities/week")
        
        # Emotions
        if emotion_data:
            primary_emotion = emotion_data.get('emotion', 'neutral')
            emotion_score = emotion_data.get('score', 0)
            
            if primary_emotion in ['sadness', 'anger', 'fear'] and emotion_score > 0.5:
                explanation.append(f"⚠️ Detected {primary_emotion} in recent entries - May need support")
            elif primary_emotion in ['joy', 'happiness'] and emotion_score > 0.5:
                explanation.append(f"✅ Positive emotional state detected")
        
        if not explanation:
            explanation.append("No significant risk factors detected")
        
        return explanation
