"""Main entry point for the Medical Simulation Suite AI.

Initializes and configures the main FastAPI application. This module serves as
the primary entry point, mounting all service-specific sub-applications
(e.g., scenarios, exams) and defining core health check endpoints.

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
from utils.common import get_knowledge_base

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# --- FastAPI Application Initialization ---
app = FastAPI(
    title="Medical Simulation Suite AI",
    description="A complete AI system for generating medical simulation scenarios and laboratory exams.",
    version="4.2.0"
)

# --- Include Sub-Applications ---
app.include_router(scenario_router)
app.include_router(exam_router)
app.include_router(report_router)
app.include_router(material_router)


# --- Core Endpoints ---
@app.get("/", summary="Main Health Check", tags=["Health"])
def root() -> Dict[str, Any]:
    """Provides a detailed health check of the main application.

    Returns:
        A dictionary containing the service status, name, version, and a
        map of available sub-application documentation endpoints.
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

    This endpoint is ideal for use by automated services like load balancers
    or uptime monitors to confirm that the application is responsive.

    Returns:
        A dictionary indicating the service status is healthy.
    """
    return {"status": "healthy", "service": "Medical Simulation Suite AI"}


# --- Main execution block ---
if __name__ == "__main__":
    logger.info("Starting Medical Simulation Suite AI...")
    knowledge_base = get_knowledge_base()
    knowledge_base.load(recreate=False)
    uvicorn.run("main:app", host="0.0.0.0", port=8001, reload=True)