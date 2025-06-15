"""FastAPI router for medical scenario generation endpoints.

This module defines the API routes related to creating and managing medical
simulation scenarios. It includes endpoints for generating full scenarios
based on user-defined parameters and for retrieving available difficulty levels.

Version: 4.3
"""

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
def generate_scenario_endpoint(request: ScenarioRequest) -> FullScenario:
    """Generates a complete medical scenario based on user specifications.

    This endpoint receives a request detailing the desired scenario and passes
    it to the scenario generation agent, which returns a fully structured
    scenario object.

    Args:
        request: An object containing the description, type, target audience,
            and difficulty level for the scenario.

    Returns:
        A complete, structured medical scenario object.
    """
    logger.info(f"Received request for the Medical Scenario Team: {request.model_dump()}")
    return generate_medical_scenario(request)


@router.get("/difficulty-levels", summary="Get Available Difficulty Levels")
def get_difficulty_levels() -> Dict[str, Any]:
    """Retrieves the available difficulty levels for scenario generation.

    Provides a list of supported difficulty levels, each with a value, label,
    and description in Italian, along with the default level.

    Returns:
        A dictionary containing a list of difficulty level objects and the
        default value.
    """
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
        "default": "Facile"
    }
