# Configuration file for Medical Simulation Suite AI
# Version 4.2 - Refactored configuration

import os
from typing import Dict, Any

from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# API Configuration
API_CONFIG: Dict[str, Any] = {
    "title": "Medical Simulation Suite AI",
    "description": "Complete AI system for generating medical simulation scenarios and laboratory exams.",
    "version": "4.2.0",
    "host": "0.0.0.0",
    "port": 8001
}

# Model Configuration
MODEL_CONFIG: Dict[str, Any] = {
    "scenario_model": "gemini-2.0-flash",
    "exam_model": "gemini-1.5-flash-latest"
}

# Logging Configuration
LOGGING_CONFIG: Dict[str, Any] = {
    "level": "INFO",
    "format": "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
}

# Environment Variables
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")

# Validate required environment variables
if not GOOGLE_API_KEY:
    raise ValueError("GOOGLE_API_KEY environment variable is required")
