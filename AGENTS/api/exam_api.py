# FastAPI endpoints for Medical Exam Generation
# Version 1.2 - Refactored to use APIRouter

import logging

from fastapi import APIRouter
from agents.exam_agents import generate_lab_exams
from models.exam_models import LabExamRequest, LabExamResponse

# Configure logging
logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/exams",
    tags=["Exams"]
)


@router.post("/generate-lab-exams", response_model=LabExamResponse, summary="Generate Laboratory Exams for a Scenario")
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
