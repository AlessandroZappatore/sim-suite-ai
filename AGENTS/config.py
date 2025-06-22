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

def _get_database_path() -> str:
    """
    Determines the database path checking multiple possible locations:
    1. Environment variables for Program Files directories
    2. Registry lookup for actual Program Files paths (Windows-generic)
    3. Development location: PROJECT_ROOT/../../database.db
    """
    import winreg
    
    # List of possible installer locations
    possible_installer_paths: list[str] = []
    
    # Method 1: Use environment variables (most reliable)
    programfiles_x86 = os.environ.get("PROGRAMFILES(X86)")
    if programfiles_x86:
        possible_installer_paths.append(os.path.join(programfiles_x86, "SimSuiteAI", "database.db"))
    
    programfiles = os.environ.get("PROGRAMFILES")
    if programfiles:
        possible_installer_paths.append(os.path.join(programfiles, "SimSuiteAI", "database.db"))
    
    # Method 2: Try to get Program Files paths from Windows Registry
    try:
        # Get Program Files path from registry
        with winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE, 
                           r"SOFTWARE\Microsoft\Windows\CurrentVersion") as key:
            try:
                programfiles_dir = winreg.QueryValueEx(key, "ProgramFilesDir")[0]
                possible_installer_paths.append(os.path.join(programfiles_dir, "SimSuiteAI", "database.db"))
            except FileNotFoundError:
                pass
            
            try:
                programfiles_x86_dir = winreg.QueryValueEx(key, "ProgramFilesDir (x86)")[0]
                possible_installer_paths.append(os.path.join(programfiles_x86_dir, "SimSuiteAI", "database.db"))
            except FileNotFoundError:
                pass
    except Exception:
        # If registry access fails, continue with other methods
        pass
      # Method 3: Check common drive locations as last resort
    drives = [os.environ.get("SYSTEMDRIVE", "C:")]
    if "C:" not in drives:
        drives.append("C:")
    
    for drive in drives:
        # Look for any folder that starts with "Program" in the drive root
        try:
            drive_root = os.path.join(drive, os.sep)
            if os.path.exists(drive_root):
                for item in os.listdir(drive_root):
                    item_path = os.path.join(drive_root, item)
                    if (os.path.isdir(item_path) and 
                        item.lower().startswith(("program", "programm", "archivo", "fichier", "arquivo"))):
                        possible_installer_paths.append(os.path.join(item_path, "SimSuiteAI", "database.db"))
        except (OSError, PermissionError):
            # Skip if we can't access the drive
            pass
    
    # Check each installer location
    for installer_path in possible_installer_paths:
        if installer_path and os.path.exists(installer_path):
            return installer_path
    
    # If no installer path found, always use development location
    dev_db_path = os.path.join(PROJECT_ROOT, "..", "..", "database.db")
    return dev_db_path

DATABASE_PATH = _get_database_path()

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