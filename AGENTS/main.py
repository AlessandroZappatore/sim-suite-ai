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

from api.exam_api import router as exam_router
from api.scenario_api import router as scenario_router
from api.medical_report_api import router as report_router
from api.mat_api import router as material_router
from utils import get_knowledge_base, get_report_knowledge_base
from config import API_CONFIG, LOGGING_CONFIG 

logging.basicConfig(level=LOGGING_CONFIG["level"], format=LOGGING_CONFIG["format"])
logger = logging.getLogger(__name__)

app = FastAPI(
    title=API_CONFIG["title"],
    description=API_CONFIG["description"],
    version=API_CONFIG["version"]
)

app.include_router(scenario_router)
app.include_router(exam_router)
app.include_router(report_router)
app.include_router(material_router)


@app.get("/", summary="Main Health Check", tags=["Health"])
def root() -> Dict[str, Any]:
    """Provides a detailed health check of the main application.

    Returns:
        A dictionary containing the service status, name, version, and a
        map of available sub-application documentation endpoints.
    """
    return {
        "status": "healthy",
        "service": API_CONFIG["title"],
        "version": API_CONFIG["version"],
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
    return {"status": "healthy", "service": API_CONFIG["title"]}


if __name__ == "__main__":
    logger.info("Starting Medical Simulation Suite AI...")
    knowledge_base = get_knowledge_base()
    knowledge_base.load(recreate=False)
    report_knowledge_base = get_report_knowledge_base()
    report_knowledge_base.load(recreate=False)
    uvicorn.run("main:app", host=API_CONFIG["host"], port=API_CONFIG["port"], reload=True)
