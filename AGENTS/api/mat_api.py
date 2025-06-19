"""FastAPI router for medical material generation endpoints.

This module defines the API routes for generating necessary materials
for a given medical simulation scenario. It includes an endpoint for
generating a list of materials and another for retrieving available
patient types.
"""

import logging
from typing import List, Dict

from fastapi import APIRouter
from agents import generate_materials
from models import MATModelRequest, MATModelResponse

logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/materials",
    tags=["Materials"]
)


@router.post("/generate-materials", response_model=List[MATModelResponse], summary="Generate Necessary Materials for Scenario")
def generate_materials_endpoint(request: MATModelRequest) -> List[MATModelResponse]:
    """Generates a list of necessary materials for a medical simulation."""
    logger.info(f"Received request to generate materials: {request.model_dump()}")
    return generate_materials(request)


@router.get("/form-options", summary="Get Available Options for Forms")
def get_form_options() -> Dict[str, List[str]]:
    """
    Retrieves the lists of available patient types dynamically from the
    request model to populate frontend selection inputs. The 'target'
    field is a free-text string and is not included here.
    """
    patient_type_field = MATModelRequest.model_fields['tipologia_paziente']
    allowed_patient_types = list(patient_type_field.annotation.__args__) if patient_type_field.annotation and hasattr(patient_type_field.annotation, '__args__') else []
    
    return {
        "patient_types": allowed_patient_types,
    }