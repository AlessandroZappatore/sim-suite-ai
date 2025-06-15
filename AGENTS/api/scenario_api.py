# FastAPI endpoints for Medical Scenario Generation
# Version 4.3 - Refactored to use APIRouter

import logging
from typing import Dict, Any

from fastapi import APIRouter
from agents.scenario_agents import generate_medical_scenario
from models.scenario_models import FullScenario, ScenarioRequest

# Configure logging
logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/scenarios",
    tags=["Scenarios"]
)


@router.post("/generate-scenario", response_model=FullScenario, summary="Generate a Full Medical Scenario")
def generate_scenario_endpoint(request: ScenarioRequest):
    """
    Generate a complete medical scenario based on the request parameters.
    
    Args:
        request: The scenario request containing:
            - description: Detailed description of the clinical scenario
            - scenario_type: Type of scenario (Quick/Advanced/Patient Simulated)
            - target: Target audience (optional)
            - difficulty: Difficulty level (Facile/Medio/Difficile) - affects complexity, complications, and clinical evolution
            
    Returns:
        FullScenario: The generated medical scenario with appropriate complexity for the specified difficulty level
        
    Examples:
        - Facile: Simple scenarios with stable parameters and basic interventions
        - Medio: Moderate complexity with 1-2 manageable complications  
        - Difficile: Complex scenarios with multiple complications, critical parameters, and rapid evolution
    """
    logger.info(f"Received request for the Medical Scenario Team: {request.model_dump()}")
    return generate_medical_scenario(request)


@router.get("/difficulty-levels", summary="Get Available Difficulty Levels")
def get_difficulty_levels() -> Dict[str, Any]:
    """Get list of available difficulty levels for medical scenarios."""
    return {
        "difficulty_levels": [
            {
                "value": "Facile",
                "label": "Facile",
                "description": "Scenario semplice con poche complicazioni, parametri stabili, evoluzione prevedibile"
            },
            {
                "value": "Medio", 
                "label": "Medio",
                "description": "Scenario con complessità moderata, 1-2 complicazioni gestibili, richiede pensiero critico"
            },
            {
                "value": "Difficile",
                "label": "Difficile", 
                "description": "Scenario complesso con multiple complicazioni, parametri critici, evoluzione rapida"
            }
        ],
        "default": "Medio"
    }