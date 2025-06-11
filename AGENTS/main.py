# Main API Application
# Orchestrates multiple AI agents and their respective APIs

import logging
from typing import Dict, Any

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv

# Import configuration (assuming you have this file)
# from config import MAIN_CONFIG 

# Import your existing agent applications
from sim_suite_ai import app as medical_app
from exam_agent import app as exam_app

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load environment variables
load_dotenv()

# --- MOCK CONFIG if you don't have config.py ---
# This is just for the example to run
MAIN_CONFIG: Dict[str, Any] = {
    "title": "SimSuite AI - Multi-Agent Platform",
    "description": "Central orchestrator for medical simulation and lab exam AI agents.",
    "version": "1.0.0",
    "host": "0.0.0.0",
    "port": 8000
}
# -----------------------------------------------


# Create main FastAPI application
main_app = FastAPI(
    title=MAIN_CONFIG["title"],
    description=MAIN_CONFIG["description"],
    version=MAIN_CONFIG["version"]
)

# Add CORS middleware
main_app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure as needed for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

main_app.mount("/medical", medical_app)
main_app.mount("/exam", exam_app)


# Health check endpoint
@main_app.get("/health", tags=["Management"])
def health_check() -> Dict[str, Any]:
    return {
        "status": "healthy",
        "services": {
            "medical_simulation": "active",
            "lab_exams": "active" # Corrected from "translation"
        }
    }

# Root endpoint with available services
@main_app.get("/", tags=["Management"])
def root() -> Dict[str, Any]:
    return {
        "message": "SimSuite AI - Multi-Agent Platform",
        "documentation": "/docs",
        "available_services": {
            "medical_simulation": "/medical/docs", # This path will still work
            "lab_exams": "/exam/docs",           # This path will still work
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:main_app", host=MAIN_CONFIG["host"], port=int(MAIN_CONFIG["port"]), reload=True)