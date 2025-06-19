"""Defines the AI agent and logic for medical materials generation.

This module contains the core components for generating lists of necessary
materials for medical simulations. It leverages agno's structured output
to ensure the generated list of materials is correctly formatted and validated.

Version: 3.2
"""

import os
import logging
import sqlite3
from typing import List, Dict, Any

from agno.agent import Agent, RunResponse
from fastapi import HTTPException

from models import MATModelRequest, MATModelResponse, MatModelListResponse
from utils import get_small_model
from config import DATABASE_PATH

logger = logging.getLogger(__name__)


materials_agent = Agent(
    name="Medical Materials Generator",
    role="An expert medical educator and simulation specialist who generates comprehensive lists of necessary materials for medical simulation scenarios, with general-purpose descriptions.",
    model=get_small_model(),
    response_model=MatModelListResponse,
    instructions=[
        "Your task is to generate a comprehensive list of necessary materials for a medical simulation scenario.",
        "All text content for 'nome' and 'descrizione' must be in **Italian**.",
        "The materials must be realistic and appropriate for the described scenario, patient type, and target audience.",
        "PRIORITY: If a material from the provided database list is suitable for the scenario, you MUST use its EXACT name.",
        "For any NEW material you create, provide a clear, specific Italian name and a GENERAL-PURPOSE description.",
        "The description should explain the material's main function in a way that is reusable for other scenarios. It must NOT be specific to the current scenario.",
        "Pay attention to the objective examination findings (esame_obiettivo) to determine WHICH materials are needed, but keep their descriptions generic.",
        "Consider the target audience (students, nurses, specialists) to adjust the complexity and type of materials selected.",
    ]
)


def create_materials_prompt(request: MATModelRequest, existing_materials: List[Dict[str, Any]]) -> str:
    """Creates the simplified prompt for the materials generation agent."""
    existing_materials_text = ""
    if existing_materials:
        existing_materials_text = "\n\n## DATABASE MATERIALS\n"
        existing_materials_text += "Here is a list of materials already available in the database. If any of these are appropriate, you MUST use their exact 'nome'.\n"
        for material in existing_materials:
            existing_materials_text += f"- {material['nome']}\n"

    return f"""
    Generate a comprehensive list of necessary materials for the following medical simulation.

    ## SCENARIO CONTEXT
    - Patient Type: {request.tipologia_paziente}
    - Scenario Description: {request.descrizione_scenario}
    - Target Audience: {request.target}
    - Objective Examination: {request.esame_obiettivo}
    {existing_materials_text}

    ## INSTRUCTIONS
    1.  Generate a list of materials needed for this simulation.
    2.  All names ('nome') and descriptions ('descrizione') must be in Italian.
    3.  If a material from the database list fits, use its exact name.
    4.  For new materials, create a specific Italian name.
    5.  CRITICAL: The description for EACH material must be GENERAL and REUSABLE for a database. Do not mention the specifics of this scenario in the description.
    """

def generate_materials(request: MATModelRequest) -> List[MATModelResponse]:
    """Generates a list of materials for a medical simulation scenario."""
    logger.info(f"Received request to generate materials for: {request.tipologia_paziente} - Target: {request.target}")

    try:
        existing_materials = get_existing_materials()
        logger.info(f"Retrieved {len(existing_materials)} existing materials from database")

        prompt = create_materials_prompt(request, existing_materials)
        
        run_response: RunResponse = materials_agent.run(prompt) # type: ignore

        validated_response = run_response.content
        
        if not isinstance(validated_response, MatModelListResponse):
            raise TypeError(f"Agent returned unexpected type: {type(validated_response)}. Expected MatModelListResponse.")

        validated_materials = validated_response.materials

        logger.info(f"Successfully generated {len(validated_materials)} materials")
        return validated_materials

    except Exception as e:
        logger.error(f"An unexpected error occurred during material generation: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate valid materials", "message": str(e)})


def get_existing_materials() -> List[Dict[str, Any]]:
    """Retrieves all existing materials from the SQLite database."""
    db_path = DATABASE_PATH
    if not os.path.exists(db_path):
        logger.warning(f"Database not found at {db_path}, returning no existing materials.")
        return []

    try:
        conn = sqlite3.connect(db_path)
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        cursor.execute("SELECT id_materiale, nome, descrizione FROM Materiale")
        materials = [dict(row) for row in cursor.fetchall()]
        conn.close()
        logger.debug(f"Found {len(materials)} materials in the database.")
        return materials
    except sqlite3.Error as e:
        logger.error(f"Database error while querying materials: {e}", exc_info=True)
        return []
    except Exception as e:
        logger.error(f"An unexpected error occurred while getting materials from DB: {e}", exc_info=True)
        return []