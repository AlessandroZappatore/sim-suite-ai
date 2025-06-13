# Agents for Medical Exam Generation
# Version 1.1 - Refactored from exam_agent.py

import json
import logging

from agno.agent import Agent
from fastapi import HTTPException

from models.exam_models import LabExamRequest, LabExamResponse
from utils.common import extract_json_from_response, get_exam_model

# Configure logging
logger = logging.getLogger(__name__)


# Agent Definition
exam_agent = Agent(
    name="Lab Exam Generator",
    role="An expert clinical pathologist who generates realistic lab results for medical simulations.",
    model=get_exam_model(),
    instructions=[
        "Your task is to generate a set of relevant laboratory exams based on a clinical scenario.",
        "All text content, including test names, categories, and interpretations, must be in **Italian**.",
        "The results must be plausible for the described pathology.",
        "For each test, provide a brief textual interpretation in the `referto` field.",
        "You must respond ONLY with a valid JSON object that strictly matches the required Pydantic schema.",
    ]
)


def create_exam_prompt(request: LabExamRequest) -> str:
    """Creates the detailed prompt for the lab exam generation agent."""
    return f"""
    Generate a set of relevant laboratory exams in JSON format for a medical simulation.

    SCENARIO CONTEXT:
    - Patient Type: {request.tipologia_paziente}
    - Scenario Description: {request.descrizione_scenario}

    INSTRUCTIONS:
    1.  Based on the scenario, create a list of pertinent lab exams.
    2.  Group the exams into logical categories (e.g., 'Ematologia', 'Coagulazione', 'Chimica Clinica', 'Emogasanalisi Arteriosa').
    3.  All names, categories, and textual reports MUST be in ITALIAN.
    4.  The values should be realistic for the given clinical picture.
    5.  For each test, you MUST include a `referto` field containing a brief clinical interpretation of the result (e.g., "Valore nella norma", "Valore critico indicativo di infiammazione", "Leggermente diminuito").
    6.  The `unita_misura` field MUST always be a string. If a test has no unit (e.g., a qualitative result like 'Positivo/Negativo'), use an empty string "" or "N/A".
    7.  Strictly adhere to the JSON schema provided below. Do NOT add any extra text or explanations outside the JSON structure.

    JSON SCHEMA TO FOLLOW:
    {json.dumps(LabExamResponse.model_json_schema(), indent=2)}

    Respond ONLY with the valid JSON object.
    """


def generate_lab_exams(request: LabExamRequest) -> LabExamResponse:
    """
    Generate laboratory exams for a medical scenario.
    
    Args:
        request: The lab exam request containing scenario description and patient type
        
    Returns:
        LabExamResponse: The generated lab exams
        
    Raises:
        HTTPException: If generation fails
    """
    logger.info(f"Received request to generate lab exams for: {request.tipologia_paziente}")
    
    try:
        # Create the prompt for the agent
        prompt = create_exam_prompt(request)
        
        # Run the agent to get the response
        agent_response = exam_agent.run(prompt)  # type: ignore
        
        # Extract and validate the JSON from the response
        exam_dict = extract_json_from_response(agent_response.content)
        
        # Validate the data with the Pydantic model
        validated_exams = LabExamResponse.model_validate(exam_dict)
        
        return validated_exams
        
    except (ValueError, json.JSONDecodeError) as e:
        logger.error(f"Data validation or extraction error: {e}", exc_info=True)
        raise HTTPException(status_code=422, detail={"error": "Generated content failed validation or parsing.", "details": str(e)})
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate lab exams", "message": str(e)})
