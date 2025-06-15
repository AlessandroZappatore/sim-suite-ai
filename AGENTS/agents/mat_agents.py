"""Defines the AI agent and logic for medical materials generation.

This module contains the core components for generating lists of necessary
materials for medical simulations. It defines a specialist agent, functions
for creating detailed prompts that incorporate existing database materials,
and the main orchestration logic that interacts with a SQLite database to
ensure data consistency and avoid duplication.

Version: 2.0
"""

import json
import logging
import os
import sqlite3
from typing import List, Dict, Any

from agno.agent import Agent
from fastapi import HTTPException
from pydantic import ValidationError

from models.mat_model import MATModelRequest, MATModelResponse
from utils.common import extract_json_from_response, get_exam_model

# Configure logging
logger = logging.getLogger(__name__)


# --- Agent Definition ---
materials_agent = Agent(
    name="Medical Materials Generator",
    role="An expert medical educator and simulation specialist who generates comprehensive lists of necessary materials for medical simulation scenarios, with general-purpose descriptions.",
    model=get_exam_model(),
    instructions=[
        "Your task is to generate a comprehensive list of necessary materials for a medical simulation scenario.",
        "The final JSON output's text content (both 'nome' and 'descrizione') must be in **Italian**.",
        "The materials must be realistic and appropriate for the described scenario, patient type, and target audience.",
        "PRIORITY: If a material from the database is suitable for the scenario, you MUST use the EXACT name from the database.",
        "For any NEW material you create, provide a clear, specific Italian name and a GENERAL-PURPOSE description.",
        "The description should explain the material's main function in a way that is reusable for other scenarios. It must NOT be specific to the current scenario.",
        "Pay particular attention to the objective examination findings (esame_obiettivo) to determine WHICH materials are needed, but do not make their descriptions specific to the exam.",
        "Consider the target audience (students, nurses, specialists) to adjust the complexity and type of materials selected.",
        "Avoid creating duplicate materials - if a material exists in the database, use its exact name.",
        "You MUST respond with a valid JSON array enclosed in ```json code blocks.",
        "The JSON must follow this exact structure: [{\"nome\": \"Material Name\", \"descrizione\": \"General Description\"}]",
        "ALWAYS wrap your JSON response in ```json and ``` code blocks.",
    ]
)


def create_materials_prompt(request: MATModelRequest, existing_materials: List[Dict[str, Any]]) -> str:
    """Creates the detailed prompt for the materials generation agent.

    This function constructs a comprehensive prompt that includes the scenario
    context and a list of existing materials from the database to guide the
    agent in its generation task.

    Args:
        request: The user's request containing the scenario details.
        existing_materials: A list of materials already present in the database.

    Returns:
        A fully formatted prompt string for the materials_agent.
    """
    existing_materials_text = ""
    if existing_materials:
        existing_materials_text = "\n    EXISTING MATERIALS IN THE DATABASE:\n"
        for material in existing_materials:
            existing_materials_text += f"    - {material['nome']}: {material.get('descrizione', 'Existing material')}\n"
        existing_materials_text += "\n    IMPORTANT: If any of these existing materials are appropriate for the scenario, use the EXACT name from the database.\n"

    return f"""
    Generate a comprehensive list of necessary materials in JSON format for a medical simulation scenario.

    SCENARIO CONTEXT:
    - Patient Type: {request.tipologia_paziente}
    - Scenario Description: {request.descrizione_scenario}
    - Target Audience: {request.target}
    - Objective Examination (Esame obiettivo): {request.esame_obiettivo}
    - Existing materials: {existing_materials_text}

    INSTRUCTIONS:
    1.  Generate a realistic and comprehensive list of materials needed for this medical simulation.
    2.  All text content in the final JSON output (names and descriptions) MUST be in ITALIAN.
    3.  PRIORITY: If you need a material that already exists in the database, use the EXACT name provided.
    4.  For new materials, create a clear and specific name in Italian.
    5.  CRITICAL: The description for EACH material must be GENERAL and REUSABLE. It should describe the material's general purpose, not its specific use in this one scenario. This description will be stored in a database for reuse.
    6.  DO NOT include details from the scenario context in the description.
    7.  Based on the objective examination, include the specific materials needed for that type of examination, but keep their descriptions generic.
    8.  Include various categories of materials: Diagnostic, Monitoring, Safety (PPE), Therapeutic, Educational, etc.
    9.  For each material, provide:
        - "nome": A clear and specific name in Italian (use existing names when available).
        - "descrizione": A general, reusable description in Italian explaining its purpose.
    10. Ensure materials are appropriate for the clinical scenario and realistic for the setting.
    11. Think about materials needed for different phases: assessment, intervention, monitoring, documentation.

    JSON SCHEMA TO FOLLOW:
    The response must be a JSON array of objects. Each object represents a material with "nome" and "descrizione" fields.

    IMPORTANT: You MUST wrap your response in ```json code blocks. The JSON should be the only content in your response.

    EXAMPLE:
    ```json
    [
        {{"nome": "Stetoscopio", "descrizione": "Strumento acustico utilizzato per l'auscultazione dei suoni interni del corpo, come il battito cardiaco e i suoni respiratori."}},
        {{"nome": "Sfigmomanometro aneroide", "descrizione": "Dispositivo per la misurazione non invasiva della pressione arteriosa, composto da un bracciale gonfiabile e un manometro."}},
        {{"nome": "Guanti monouso non sterili", "descrizione": "Dispositivi di protezione individuale per le mani, utilizzati per prevenire la contaminazione durante l'esame del paziente o la manipolazione di materiali."}}
    ]
    ```

    Remember:
    - Start with ```json
    - End with ```
    - The content of 'nome' and 'descrizione' must be in Italian.
    - The 'descrizione' must be generic and reusable.
    """


def generate_materials(request: MATModelRequest) -> List[MATModelResponse]:
    """Generates a list of materials for a medical simulation scenario.

    This function orchestrates the generation process by retrieving existing
    materials from a database, creating a detailed prompt, running an AI agent,
    and validating the final output.

    Args:
        request: The request containing the scenario, patient type, target
            audience, and objective exam findings.

    Returns:
        A list of generated and validated materials.

    Raises:
        HTTPException: If the generation, parsing, or validation process fails.
    """
    logger.info(f"Received request to generate materials for: {request.tipologia_paziente} - Target: {request.target}")

    try:
        existing_materials = get_existing_materials()
        logger.info(f"Retrieved {len(existing_materials)} existing materials from database")

        prompt = create_materials_prompt(request, existing_materials)
        agent_response = materials_agent.run(prompt) # type: ignore
        materials_list = extract_json_from_response(agent_response.content)

        if not isinstance(materials_list, list):
            raise ValueError("Expected a list of materials from the agent.")

        validated_materials: List[MATModelResponse] = [
            MATModelResponse.model_validate(material) for material in materials_list
        ]

        logger.info(f"Successfully generated {len(validated_materials)} materials")
        return validated_materials

    except (ValidationError, ValueError, json.JSONDecodeError) as e:
        logger.error(f"Data validation or extraction error: {e}", exc_info=True)
        raise HTTPException(status_code=422, detail={"error": "Generated content failed validation or parsing.", "details": str(e)})
    except Exception as e:
        logger.error(f"An unexpected error occurred during material generation: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate materials", "message": str(e)})


def get_database_path() -> str:
    """Constructs and returns the absolute path to the SQLite database file.

    Returns:
        The absolute path to the 'database.db' file.
    """
    current_dir = os.path.dirname(os.path.abspath(__file__))
    # Assumes the database is in the project root, three levels up from agents/
    root_dir = os.path.dirname(os.path.dirname(os.path.dirname(current_dir)))
    return os.path.join(root_dir, "database.db")


def get_existing_materials() -> List[Dict[str, Any]]:
    """Retrieves all existing materials from the SQLite database.

    Connects to the database, queries the 'Materiale' table, and returns a
    list of all materials.

    Returns:
        A list of dictionaries, where each dictionary represents a material
        with its ID, name, and description. Returns an empty list on failure.
    """
    db_path = get_database_path()
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
        logger.info(f"Found {len(materials)} materials in the database.")
        return materials
    except sqlite3.Error as e:
        logger.error(f"Database error while querying materials: {e}", exc_info=True)
        return []
    except Exception as e:
        logger.error(f"An unexpected error occurred while getting materials from DB: {e}", exc_info=True)
        return []