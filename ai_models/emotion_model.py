"""
Emotion detection model using Hugging Face Transformers
"""

from transformers import pipeline
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class EmotionAnalyzer:
    """
    Emotion analyzer using pre-trained transformer model from Hugging Face
    """
    
    def __init__(self, model_name="j-hartmann/emotion-english-distilroberta-base"):
        """
        Initialize the emotion analyzer
        
        Args:
            model_name: Hugging Face model identifier
        """
        logger.info(f"Loading emotion model: {model_name}")
        try:
            self.classifier = pipeline(
                "text-classification",
                model=model_name,
                top_k=None  # Return all emotion scores
            )
            logger.info("Emotion model loaded successfully")
        except Exception as e:
            logger.error(f"Error loading emotion model: {e}")
            raise
    
    def analyze(self, text):
        """
        Analyze emotion in text
        
        Args:
            text: Input text to analyze
            
        Returns:
            dict: {
                'emotion': str (primary emotion),
                'score': float (confidence score 0-1),
                'all_emotions': list of dicts with all emotions and scores
            }
        """
        if not text or not text.strip():
            return {
                'emotion': 'neutral',
                'score': 0.0,
                'all_emotions': []
            }
        
        try:
            # Get predictions
            results = self.classifier(text[:512])  # Limit text length to 512 tokens
            
            if isinstance(results, list) and len(results) > 0:
                # If top_k=None, results[0] contains list of all emotions
                if isinstance(results[0], list):
                    all_emotions = results[0]
                else:
                    all_emotions = [results[0]]
                
                # Sort by score
                all_emotions = sorted(all_emotions, key=lambda x: x['score'], reverse=True)
                
                # Get primary emotion
                primary = all_emotions[0]
                
                return {
                    'emotion': primary['label'].lower(),
                    'score': round(primary['score'], 4),
                    'all_emotions': [
                        {
                            'emotion': e['label'].lower(),
                            'score': round(e['score'], 4)
                        }
                        for e in all_emotions
                    ]
                }
            else:
                return {
                    'emotion': 'neutral',
                    'score': 0.0,
                    'all_emotions': []
                }
                
        except Exception as e:
            logger.error(f"Error analyzing emotion: {e}")
            return {
                'emotion': 'error',
                'score': 0.0,
                'all_emotions': [],
                'error': str(e)
            }
    
    def get_mood_score(self, emotion_data):
        """
        Convert emotion to mood score (1-10 scale)
        
        Args:
            emotion_data: Output from analyze() method
            
        Returns:
            float: Mood score from 1 (very negative) to 10 (very positive)
        """
        # Emotion to mood score mapping
        emotion_scores = {
            'joy': 9.0,
            'happiness': 9.0,
            'love': 8.5,
            'surprise': 7.0,
            'neutral': 6.5,
            'fear': 3.5,
            'sadness': 2.5,
            'anger': 2.0,
            'disgust': 2.0,
            'anxiety': 3.0,
            'disappointment': 3.5
        }
        
        emotion = emotion_data.get('emotion', 'neutral')
        base_score = emotion_scores.get(emotion, 5.0)
        
        # Adjust based on confidence
        confidence = emotion_data.get('score', 0.5)
        
        # If confidence is low, move towards neutral
        if confidence < 0.5:
            base_score = base_score * confidence + 5.0 * (1 - confidence)
        
        return round(base_score, 2)
