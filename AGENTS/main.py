"""Main entry point for the Medical Simulation Suite AI.

This module initializes the main FastAPI application and mounts all the
sub-applications (APIs) for different services like scenarios, exams,
and medical reports. It also defines the main health check endpoints.

Version: 4.2.0
"""

import logging
from typing import Dict, Any
from fastapi import FastAPI
import uvicorn

# Import sub-applications from the api module
from api.exam_api import router as exam_router
from api.scenario_api import router as scenario_router
from api.medical_report_api import router as report_router
from api.mat_api import router as material_router
# --- Basic Configuration ---
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# --- FastAPI Application Initialization ---
app = FastAPI(
    title="Medical Simulation Suite AI",
    description="A complete AI system for generating medical simulation scenarios and laboratory exams.",
    version="4.2.0"
)

# --- Mount Sub-Applications ---
# Each sub-application handles a specific domain of the suite.
app.include_router(scenario_router)
app.include_router(exam_router)
app.include_router(report_router)
app.include_router(material_router)


# --- Core Endpoints ---
@app.get("/", summary="Main Health Check", tags=["Health"])
def root() -> Dict[str, Any]:
    """Provides a detailed health check of the main application.

    This root endpoint returns a JSON object containing the operational status,
    service name, version, and a dictionary of available sub-application
    documentation endpoints.

    Returns:
        Dict[str, Any]: A dictionary with service status and essential metadata.
    """
    return {
        "status": "healthy",
        "service": "Medical Simulation Suite AI",
        "version": "4.2.0",
        "endpoints": {
            "scenarios": "/scenarios/docs",
            "exams": "/exams/docs",
            "reports": "/reports/docs",
            "materials": "/materials/docs"
        }
    }


@app.get("/health", summary="Simple Health Check", tags=["Health"])
def health_check() -> Dict[str, str]:
    """Provides a simple health check for monitoring services.

    This endpoint is typically used by load balancers, uptime monitors,
    or other automated services to verify that the application is running
    and responsive.

    Returns:
        Dict[str, str]: A dictionary indicating the service is healthy.
    """
    return {"status": "healthy", "service": "Medical Simulation Suite AI"}


# --- Main execution block ---
if __name__ == "__main__":
    """Main execution block to run the Uvicorn server.

    This block is executed when the script is run directly (e.g., `python main.py`).
    It starts the Uvicorn web server, making the API available. The `reload=True`
    parameter enables auto-reloading for development, where the server restarts
    after code changes.
    """
    logger.info("Starting Medical Simulation Suite AI...")
    uvicorn.run("main:app", host="0.0.0.0", port=8001, reload=True)