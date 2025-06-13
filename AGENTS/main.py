# Main entry point for Medical Simulation Suite AI
# Version 4.2 - Refactored and combined APIs

import logging

from typing import Dict, Any
from fastapi import FastAPI

from api.exam_api import exam_app
from api.scenario_api import scenario_app
from api.medical_report_api import medical_report_app

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Create main FastAPI application
app = FastAPI(
    title="Medical Simulation Suite AI",
    description="Complete AI system for generating medical simulation scenarios and laboratory exams.",
    version="4.2.0"
)

# Mount the sub-applications
app.mount("/scenarios", scenario_app)
app.mount("/exams", exam_app)
app.mount("/reports", medical_report_app)

@app.get("/", summary="Main Health Check")
def root() -> Dict[str, Any]:
    """Main health check endpoint."""
    return {
        "status": "healthy", 
        "service": "Medical Simulation Suite AI",
        "version": "4.2.0",        "endpoints": {
            "scenarios": "/scenarios/docs",
            "exams": "/exams/docs",
            "reports": "/reports/docs"
        }
    }

@app.get("/health", summary="Health Check")
def health_check():
    """Health check endpoint."""
    return {"status": "healthy", "service": "Medical Simulation Suite AI"}

# For standalone execution
if __name__ == "__main__":
    import uvicorn
    logger.info("Starting Medical Simulation Suite AI...")
    uvicorn.run("main:app", host="0.0.0.0", port=8001, reload=True)