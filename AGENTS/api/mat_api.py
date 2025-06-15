"""FastAPI router for medical material generation endpoints.

This module defines the API routes for generating necessary materials
for a given medical simulation scenario. It includes an endpoint for
generating a list of materials and another for retrieving available
patient and target audience types.
"""

import logging
from typing import List, Dict

from fastapi import APIRouter
from agents.mat_agents import generate_materials
from models.mat_model import MATModelRequest, MATModelResponse

# Configure logging
logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/materials",
    tags=["Materials"]
)


@router.post("/generate-materials", response_model=List[MATModelResponse], summary="Generate Necessary Materials for Scenario")
def generate_materials_endpoint(request: MATModelRequest) -> List[MATModelResponse]:
    """Generates a list of necessary materials for a medical simulation.

    Args:
        request: A request object containing the scenario description,
            patient type, target audience, and objective exam findings.

    Returns:
        A list of generated materials, each with a name and description.
    """
    logger.info(f"Received request to generate materials: {request.model_dump()}")
    return generate_materials(request)


@router.get("/patient-types", summary="Get Available Patient Types")
def get_patient_types() -> Dict[str, List[str]]:
    """Retrieves the lists of available patient types and target audiences.

    This endpoint provides supported values for creating material generation
    requests, which can be used to populate frontend selection inputs.

    Returns:
        A dictionary containing lists of supported 'patient_types' and
        'target_audiences'.
    """
    return {
        "patient_types": ["Adulto", "Pediatrico", "Neonatale", "Prematuro"],
        "target_audiences": [
            "Studenti di Medicina",
            "Infermieri",
            "Medici Specialisti",
            "Medici di Base",
            "Studenti di Infermieristica",
            "Operatori Sanitari"
        ]
    }
