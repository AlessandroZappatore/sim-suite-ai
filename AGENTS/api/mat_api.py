import logging
from typing import List

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
    """
    Receives a scenario description and generates a list of necessary materials for the medical simulation.
    
    Args:
        request: The materials request containing scenario description, patient type, target audience, and objective exam
        
    Returns:
        List[MATModelResponse]: The list of generated materials with names and descriptions
    """
    return generate_materials(request)


@router.get("/patient-types", summary="Get Available Patient Types")
def get_patient_types():
    """Get list of available patient types for material generation."""
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