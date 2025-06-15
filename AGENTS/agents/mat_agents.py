# Agent for Medical Materials Generation
# Version 2.0 - Generates reusable medical materials

import json
import logging
import os
import sqlite3
from typing import List, Dict, Any

from agno.agent import Agent
from fastapi import HTTPException

from models.mat_model import MATModelRequest, MATModelResponse
from utils.common import extract_json_from_response, get_exam_model

# Configure logging
logger = logging.getLogger(__name__)


# Agent Definition
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
    """Creates the detailed prompt for the materials generation agent."""
    
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
    """
    Generate a list of necessary materials for a medical simulation scenario.
    
    Args:
        request: The materials request containing scenario, patient type, target audience, and objective exam
        
    Returns:
        List[MATModelResponse]: The list of generated materials
        
    Raises:
        HTTPException: If generation fails
    """
    logger.info(f"Received request to generate materials for: {request.tipologia_paziente} - Target: {request.target}")
    
    try:
        # Get existing materials from the database
        existing_materials = get_existing_materials()
        logger.info(f"Retrieved {len(existing_materials)} existing materials from database")
        
        # Create the prompt for the agent with existing materials
        prompt = create_materials_prompt(request, existing_materials)
        
        # Run the agent to get the response
        agent_response = materials_agent.run(prompt)  # type: ignore
        
        # Extract and validate the JSON from the response
        materials_list = extract_json_from_response(agent_response.content)
        
        # Validate that we have a list
        if not isinstance(materials_list, list):
            raise ValueError("Expected a list of materials")
        
        # Validate each material with the Pydantic model
        validated_materials: List[MATModelResponse] = []
        for material_dict in materials_list:
            # This now expects `{"nome": "...", "descrizione": "..."}`
            validated_material = MATModelResponse.model_validate(material_dict)
            validated_materials.append(validated_material)
        
        logger.info(f"Successfully generated {len(validated_materials)} materials")
        return validated_materials
        
    except (ValueError, json.JSONDecodeError) as e:
        logger.error(f"Data validation or extraction error: {e}", exc_info=True)
        raise HTTPException(status_code=422, detail={"error": "Generated content failed validation or parsing.", "details": str(e)})
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate materials", "message": str(e)})


# --- NESSUNA MODIFICA NECESSARIA NELLE FUNZIONI SEGUENTI ---

def get_database_path() -> str:
    """Get the path to the SQLite database."""
    # The database is in the root of the project (one more level up)
    current_dir = os.path.dirname(os.path.abspath(__file__))
    root_dir = os.path.dirname(os.path.dirname(os.path.dirname(current_dir)))  
    return os.path.join(root_dir, "database.db")


def get_existing_materials() -> List[Dict[str, Any]]:
    """
    Retrieve existing materials from the SQLite database.
    
    Returns:
        List[Dict[str, Any]]: List of existing materials with their names and descriptions
    """
    try:
        db_path = get_database_path()
        if not os.path.exists(db_path):
            logger.warning(f"Database not found at {db_path}")
            return []
        
        conn = sqlite3.connect(db_path)
        # Use a dictionary cursor to get column names automatically
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        
        try:
            cursor.execute("SELECT id_materiale, nome, descrizione FROM Materiale")
            rows = cursor.fetchall()
            
            # Convert rows to dictionaries
            materials: List[Dict[str, Any]] = [dict(row) for row in rows]
            
            logger.info(f"Found {len(materials)} materials in Materiale table")
            conn.close()
            return materials
            
        except sqlite3.Error as e:
            logger.error(f"Error querying Materiale table: {e}")
            # Fallback to discover table structure
            tables_query = "SELECT name FROM sqlite_master WHERE type='table';"
            cursor.execute(tables_query)
            tables = cursor.fetchall()
            logger.info(f"Available tables in database: {[table['name'] for table in tables]}")
            
            conn.close()
            return []
        
    except Exception as e:
        logger.error(f"Error retrieving materials from database: {e}")
        return []


def test_database_connection() -> bool:
    """
    Test the database connection and verify the Materiale table structure.
    
    Returns:
        bool: True if connection successful and table exists, False otherwise
    """
    try:
        db_path = get_database_path()
        logger.info(f"Testing database connection at: {db_path}")
        
        if not os.path.exists(db_path):
            logger.error(f"Database file not found at {db_path}")
            return False
        
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # Check if Materiale table exists
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='Materiale'")
        table_exists = cursor.fetchone()
        
        if not table_exists:
            logger.error("Materiale table not found in database")
            # List available tables for debugging
            cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
            tables = cursor.fetchall()
            logger.info(f"Available tables: {[table[0] for table in tables]}")
            conn.close()
            return False
        
        # Check table structure
        cursor.execute("PRAGMA table_info(Materiale)")
        columns = cursor.fetchall()
        column_names = [col[1] for col in columns]
        
        expected_columns = ['id_materiale', 'nome', 'descrizione']
        missing_columns = [col for col in expected_columns if col not in column_names]
        
        if missing_columns:
            logger.error(f"Missing columns in Materiale table: {missing_columns}")
            logger.info(f"Available columns: {column_names}")
            conn.close()
            return False
        
        # Test a simple query
        cursor.execute("SELECT COUNT(*) FROM Materiale")
        count = cursor.fetchone()[0]
        logger.info(f"Database connection successful. Found {count} materials in Materiale table")
        
        conn.close()
        return True
        
    except Exception as e:
        logger.error(f"Database connection test failed: {e}")
        return False