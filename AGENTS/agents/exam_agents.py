"""Defines the AI agent and logic for medical exam generation.

This module contains the core components for the AI-driven lab exam
creation process. It defines a specialist agent for generating lab results,
a function to create a detailed prompt, and the main function that
orchestrates the generation and validation pipeline.

Version: 1.1
"""

import json
import logging

from agno.agent import Agent
from fastapi import HTTPException
from pydantic import ValidationError

from models.exam_models import LabExamRequest, LabExamResponse
from utils.common import extract_json_from_response, get_exam_model

# Configure logging
logger = logging.getLogger(__name__)


# --- Agent Definition ---
exam_agent = Agent(
    name="Lab Exam Generator",
    role="An expert clinical pathologist who generates realistic lab results for medical simulations.",
    model=get_exam_model(),
    instructions=[
        "Your task is to generate a set of relevant laboratory exams based on a clinical scenario.",
        "All text content, including test names, categories, and interpretations, must be in **Italian**.",
        "The results must be plausible for the described pathology.",
        "Pay particular attention to the objective examination findings (esame_obiettivo) to select appropriate lab tests.",
        "Use the objective examination information to guide which laboratory tests would be most relevant and clinically indicated.",
        "Ensure the lab results are consistent with the clinical examination findings described in esame_obiettivo.",
        "For each test, provide a brief textual interpretation in the `referto` field that correlates with the clinical scenario.",
        "Consider how the objective examination findings might influence the expected lab values.",
        "You must respond ONLY with a valid JSON object that strictly matches the required Pydantic schema.",
    ]
)


def create_exam_prompt(request: LabExamRequest) -> str:
    """Creates the detailed prompt for the lab exam generation agent.

    Args:
        request: The user's request containing the clinical scenario,
            patient type, and objective exam findings.

    Returns:
        A fully formatted prompt string to be sent to the exam_agent.
    """
    return f"""
    Generate a set of relevant laboratory exams in JSON format for a medical simulation.

    SCENARIO CONTEXT:
    - Patient Type: {request.tipologia_paziente}
    - Scenario Description: {request.descrizione_scenario}
    - Objective Examination (Esame obiettivo): {request.esame_obiettivo}

    INSTRUCTIONS:
    1.  Based on the scenario and objective examination findings, create a list of pertinent lab exams.
    2.  Use the objective examination results to guide the selection of appropriate laboratory tests.
    3.  Consider which lab tests would be most clinically relevant given the physical examination findings.
    4.  Group the exams into logical categories (e.g., 'Ematologia', 'Coagulazione', 'Chimica Clinica', 'Emogasanalisi Arteriosa').
    5.  All names, categories, and textual reports MUST be in ITALIAN.
    6.  The values should be realistic for the given clinical picture and consistent with the objective examination.
    7.  For each test, you MUST include a `referto` field containing a brief clinical interpretation of the result that correlates with the clinical scenario and objective examination (e.g., "Valore nella norma", "Valore critico indicativo di infiammazione", "Leggermente diminuito compatibile con il quadro clinico").
    8.  The `unita_misura` field MUST always be a string. If a test has no unit (e.g., a qualitative result like 'Positivo/Negativo'), use an empty string "" or "N/A".
    9.  Ensure the lab results are coherent with the physical examination findings described in the esame_obiettivo.
    10. Strictly adhere to the JSON schema provided below. Do NOT add any extra text or explanations outside the JSON structure.

    JSON SCHEMA TO FOLLOW:
    {json.dumps(LabExamResponse.model_json_schema(), indent=2)}

    Respond ONLY with the valid JSON object.
    """


def generate_lab_exams(request: LabExamRequest) -> LabExamResponse:
    """Generates laboratory exams for a medical scenario.

    This function orchestrates the exam generation process by creating a
    prompt, running the agent, and validating the final output.

    Args:
        request: The lab exam request containing the scenario description
            and patient type.

    Returns:
        The generated and validated laboratory exams.

    Raises:
        HTTPException: If the generation, parsing, or validation fails.
    """
    logger.info(f"Received request to generate lab exams for: {request.tipologia_paziente}")

    try:
        # Create the prompt for the agent
        prompt = create_exam_prompt(request)

        # Run the agent to get the response
        agent_response = exam_agent.run(prompt) # type: ignore

        # Extract and validate the JSON from the response
        exam_dict = extract_json_from_response(agent_response.content)

        # Validate the data with the Pydantic model
        validated_exams = LabExamResponse.model_validate(exam_dict)

        logger.info(f"Successfully generated lab exams for {request.tipologia_paziente}")
        return validated_exams

    except (ValidationError, ValueError, json.JSONDecodeError) as e:
        logger.error(f"Data validation or extraction error: {e}", exc_info=True)
        raise HTTPException(status_code=422, detail={"error": "Generated content failed validation or parsing.", "details": str(e)})
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate lab exams", "message": str(e)})
