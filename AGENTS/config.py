"""Configuration settings for the Medical Simulation Suite AI.

This module centralizes application configuration, including API metadata,
model selections, and logging settings. It loads sensitive credentials
from environment variables and determines the active AI provider.
"""

import os
from typing import Dict, Any
from dotenv import load_dotenv

load_dotenv()

API_CONFIG: Dict[str, Any] = {
    "title": "Medical Simulation Suite AI",
    "description": "Complete AI system for generating medical simulation scenarios and laboratory exams.",
    "version": "4.3.0",
    "host": "0.0.0.0",
    "port": 8001
}

MODEL_CONFIG: Dict[str, Any] = {
    "gemini_new": "gemini-2.5-flash",
    "gemini_big": "gemini-2.0-flash", 
    "gemini_small": "gemini-1.5-flash-latest",
    "claude_big": "claude-3-7-sonnet-20250219", 
    "claude_small": "claude-3-5-haiku-20241022",
}

LOGGING_CONFIG: Dict[str, Any] = {
    "level": "INFO",
    "format": "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
}

PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
DATABASE_PATH = os.path.join(PROJECT_ROOT, "..", "..", "database.db")

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY")

def _determine_ai_provider():
    if GOOGLE_API_KEY:
        return "Google"
    elif ANTHROPIC_API_KEY:
        return "Anthropic"
    else:
        raise ValueError("Nessuna chiave API trovata. Impostare GOOGLE_API_KEY o ANTHROPIC_API_KEY.")

AI_PROVIDER = _determine_ai_provider()

USE_ANTHROPIC = (AI_PROVIDER == "Anthropic")

DIFFICULTY_LEVELS_CONFIG: Dict[str, Any] = {
    "difficulty_levels": [
        {
            "value": "Facile",
            "label": "Facile",
            "description": "Scenario semplice con poche complicazioni, parametri stabili, evoluzione prevedibile"
        },
        {
            "value": "Medio",
            "label": "Medio",
            "description": "Scenario con complessità moderata, 1-2 complicazioni gestibili, richiede pensiero critico"
        },
        {
            "value": "Difficile",
            "label": "Difficile",
            "description": "Scenario complesso con multiple complicazioni, parametri critici, evoluzione rapida"
        }
    ],
    "default": "Facile"
}

PRESIDI_MEDICI = [
    "Defibrillatore",
    "Collare cervicale",
    "Cannula orofaringea",
    "Pulsossimetro",
    "Tavola spinale",
    "Maschera di ventilazione",
    "Barelle (a cucchiaio, autocaricante)",
    "Fasce triangolari",
    "Bende di fissaggio",
    "Stecche (rigide, modellabili)",
    "Trazione in linea",
    "Cannula nasofaringea",
    "Pallone Ambu (o ventilatore manuale)",
    "Telino sterile",
    "Laccio emostatico",
    "Medicazioni emostatiche",
    "Bende (di vario tipo e dimensione)",
    "Cerotti",
    "Garze sterili",
    "Cotone idrofilo",
    "Sfigmomanometro",
    "Termometro",
    "Aghi e siringhe",
    "Cannule intravenose",
    "Set per infusione",
    "Catetere vescicale",
    "Sonde nasogastriche",
    "Elettrodi per ECG",
    "Maschere per ossigeno",
    "Aerosol/nebulizzatore"
]