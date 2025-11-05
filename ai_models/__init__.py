"""
AI Models package for emotion detection and dropout risk prediction
"""

from .emotion_model import EmotionAnalyzer
from .tabular_model import DropoutRiskPredictor

__all__ = ['EmotionAnalyzer', 'DropoutRiskPredictor']
