# FastAPI endpoints for Medical Exam Generation
# Version 1.1 - Refactored from exam_agent.py

import logging

from fastapi import FastAPI

from agents.exam_agents import generate_lab_exams
from models.exam_models import LabExamRequest, LabExamResponse

# Configure logging
logger = logging.getLogger(__name__)

# Create FastAPI application
exam_app = FastAPI(
    title="Lab Exam Generation Agent",
    description="An AI agent that generates laboratory exams for medical simulations.",
    version="1.1.0"
)


@exam_app.post("/generate-lab-exams", response_model=LabExamResponse, summary="Generate Laboratory Exams for a Scenario")
def generate_exams_endpoint(request: LabExamRequest):
    """
    Receives a scenario description and generates a corresponding set of lab results,
    including a textual report for each test.
    
    Args:
        request: The lab exam request containing scenario description and patient type
        
    Returns:
        LabExamResponse: The generated laboratory exams
    """
    return generate_lab_exams(request)


@exam_app.get("/health", summary="Health Check")
def health_check():
    """Health check endpoint for the exam API."""
    return {"status": "healthy", "service": "Lab Exam Generator"}
