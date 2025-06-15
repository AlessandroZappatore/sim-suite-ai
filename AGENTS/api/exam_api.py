"""FastAPI router for medical exam generation endpoints.

This module defines the API routes for generating laboratory exam results
based on a given clinical scenario. It provides an endpoint to create a
structured set of lab results.

Version: 1.2
"""

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
def generate_exams_endpoint(request: LabExamRequest) -> LabExamResponse:
    """Generates a set of lab results based on a clinical scenario.

    This endpoint receives a scenario description and generates a corresponding
    set of lab results, including a textual interpretation for each test.

    Args:
        request: A request object containing the clinical scenario description
            and patient type.

    Returns:
        A response object containing the generated laboratory exams.
    """
    logger.info(f"Received request to generate lab exams: {request.model_dump()}")
    return generate_lab_exams(request)
