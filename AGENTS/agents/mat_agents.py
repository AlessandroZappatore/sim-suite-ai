# Agent for Medical Materials Generation
# Version 1.0 - Medical materials generation agent

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
    role="An expert medical educator and simulation specialist who generates comprehensive lists of necessary materials for medical simulation scenarios.",
    model=get_exam_model(),    instructions=[
        "Your task is to generate a comprehensive list of necessary materials for medical simulation scenarios.",
        "All text content must be in **Italian**.",
        "The materials must be realistic and appropriate for the described scenario, patient type, and target audience.",
        "PRIORITÀ: Se esistono materiali nel database che sono appropriati per lo scenario, usa ESATTAMENTE il nome presente nel database.",
        "Per i materiali esistenti nel database, puoi adattare la descrizione al contesto specifico dello scenario.",
        "Se hai bisogno di materiali non presenti nel database, creane di nuovi con nomi chiari e specifici in italiano.",
        "Pay particular attention to the objective examination findings (esame_obiettivo) to determine what materials would be needed for that specific examination.",
        "Consider the target audience (students, nurses, specialists) to adjust the complexity and type of materials needed.",
        "Include both basic and advanced materials as appropriate for the scenario.",
        "For each material, provide a clear name and detailed description of its purpose in the scenario.",
        "Consider safety materials, diagnostic tools, therapeutic equipment, monitoring devices, and educational resources.",
        "Avoid creating duplicate materials - if a material exists in the database, use that exact name.",
        "You MUST respond with a valid JSON array enclosed in ```json code blocks.",
        "The JSON must follow this exact structure: [{\"nome\": \"Material Name\", \"descrizione_scenario\": \"Description\"}]",
        "ALWAYS wrap your JSON response in ```json and ``` code blocks.",
    ]
)


def create_materials_prompt(request: MATModelRequest, existing_materials: List[Dict[str, Any]]) -> str:
    """Creates the detailed prompt for the materials generation agent."""
    
    # Create a formatted list of existing materials
    existing_materials_text = ""
    if existing_materials:
        existing_materials_text = "\n    MATERIALI ESISTENTI NEL DATABASE:\n"
        for material in existing_materials:
            existing_materials_text += f"    - {material['nome']}: {material.get('descrizione', 'Materiale esistente')}\n"
        existing_materials_text += "\n    IMPORTANTE: Se uno dei materiali esistenti è appropriato per lo scenario, usa ESATTAMENTE il nome presente nel database.\n"
    
    return f"""
    Generate a comprehensive list of necessary materials in JSON format for a medical simulation scenario.

    SCENARIO CONTEXT:
    - Patient Type: {request.tipologia_paziente}
    - Scenario Description: {request.descrizione_scenario}
    - Target Audience: {request.target}
    - Objective Examination (Esame obiettivo): {request.esame_obiettivo}
    {existing_materials_text}
    INSTRUCTIONS:
    1. Generate a realistic and comprehensive list of materials needed for this medical simulation.
    2. All text content MUST be in ITALIAN.
    3. PRIORITÀ: Se hai bisogno di un materiale che esiste già nel database, usa ESATTAMENTE il nome presente nel database.
    4. Per i materiali esistenti, puoi adattare la descrizione al contesto specifico dello scenario.
    5. Se hai bisogno di materiali non presenti nel database, creane di nuovi con nomi chiari e specifici.
    6. Consider the patient type to determine age-appropriate materials and equipment sizes.
    7. Use the target audience to adjust the complexity and educational level of materials.
    8. Based on the objective examination, include specific materials needed for that type of examination.
    9. Include various categories of materials:
       - Diagnostic equipment (stethoscope, thermometer, etc.)
       - Monitoring devices (pulse oximeter, blood pressure cuff, etc.)
       - Safety equipment (gloves, masks, hand sanitizer, etc.)
       - Therapeutic materials (medications, IV supplies, etc.)
       - Educational resources (guidelines, protocols, reference materials)
       - Simulation-specific equipment (manikins, simulators, etc.)
    10. For each material, provide:
        - A clear and specific name in Italian (use existing names when available)
        - A detailed description explaining its purpose and use in the scenario
    11. Ensure materials are appropriate for the clinical scenario and realistic for the setting.
    12. Consider both basic and advanced materials based on the scenario complexity.
    13. Think about materials needed for different phases: assessment, intervention, monitoring, documentation.

    MATERIAL CATEGORIES TO CONSIDER:
    - Dispositivi di protezione individuale (DPI)
    - Strumenti diagnostici
    - Dispositivi di monitoraggio
    - Materiali per procedure
    - Farmaci e soluzioni
    - Materiali educativi
    - Attrezzature di simulazione
    - Materiali per documentazione    JSON SCHEMA TO FOLLOW:
    The response should be a JSON array of objects, where each object represents a material with "nome" and "descrizione_scenario" fields.    IMPORTANT: You MUST wrap your response in ```json code blocks like this:
    ```json
    [
        {{"nome": "Stetoscopio", "descrizione_scenario": "Necessario per l'auscultazione cardiaca e polmonare del paziente durante l'esame obiettivo..."}},
        {{"nome": "Sfigmomanometro", "descrizione_scenario": "Utilizzato per misurare la pressione arteriosa del paziente..."}}
    ]
    ```

    Remember: 
    - Start with ```json
    - End with ```
    - Include only the JSON array, no additional text outside the code blocks
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
        cursor = conn.cursor()
        
        # Query the Materiale table with the known structure
        try:
            cursor.execute("SELECT id_materiale, nome, descrizione FROM Materiale")
            rows = cursor.fetchall()
            
            materials: List[Dict[str, Any]] = []
            for row in rows:
                material: Dict[str, Any] = {
                    "id_materiale": row[0],
                    "nome": row[1],
                    "descrizione": row[2] if row[2] else "Materiale esistente"
                }
                materials.append(material)
            
            logger.info(f"Found {len(materials)} materials in Materiale table")
            conn.close()
            return materials
            
        except sqlite3.Error as e:
            logger.error(f"Error querying Materiale table: {e}")
            # Fallback: try to discover table structure
            tables_query = "SELECT name FROM sqlite_master WHERE type='table';"
            cursor.execute(tables_query)
            tables = cursor.fetchall()
            logger.info(f"Available tables in database: {[table[0] for table in tables]}")
            
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
