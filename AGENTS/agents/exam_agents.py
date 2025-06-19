"""Defines the AI agent and logic for medical exam generation.

This module contains the core components for the AI-driven lab exam
creation process. It leverages agno's structured output capabilities
to generate and validate lab results directly against Pydantic models.

Version: 2.1 (Fixed return type from agent run)
"""

import logging
from agno.agent import Agent, RunResponse
from fastapi import HTTPException

# Assumiamo che questi import funzionino come prima
from models.exam_models import LabExamRequest, LabExamResponse
from utils.common import get_small_model, get_knowledge_base

# Configure logging
logger = logging.getLogger(__name__)

# --- Agent Definition (Refactored) ---
exam_agent = Agent(
    name="Lab Exam Generator",
    role="An expert clinical pathologist who generates realistic lab results for medical simulations.",
    model=get_small_model(),
    knowledge=get_knowledge_base(),
    response_model=LabExamResponse,
    instructions=[
        "Your task is to generate a set of relevant laboratory exams based on a clinical scenario.",
        "All text content, including test names, categories, and interpretations, must be in **Italian**.",
        "The results must be plausible for the described pathology.",
        "Pay particular attention to the objective examination findings (esame_obiettivo) to select appropriate lab tests.",
        "Use the objective examination information to guide which laboratory tests would be most relevant and clinically indicated.",
        "Ensure the lab results are consistent with the clinical examination findings.",
        "For each test, provide a brief textual interpretation in the `referto` field that correlates with the clinical scenario.",
        "You will be provided with a user scenario and potentially some retrieved clinical context (RAG). You MUST use the RAG context as your primary source of truth for any values it provides."
    ]
)


def create_exam_prompt(request: LabExamRequest) -> str:
    """Creates the simplified prompt for the lab exam generation agent.

    Args:
        request: The user's request containing the clinical scenario.

    Returns:
        A formatted prompt string containing the essential clinical information.
    """
    return f"""
    Generate a set of relevant laboratory exams for the following medical simulation.

    ## User-Provided Scenario ##
    - Patient Type: {request.tipologia_paziente}
    - Scenario Description: {request.descrizione_scenario}
    - Objective Examination: {request.esame_obiettivo}
    - Pathology (if specified): {request.patologia if request.patologia else 'N/A'}
    """

def generate_lab_exams(request: LabExamRequest) -> LabExamResponse:
    """Generates and validates laboratory exams for a medical scenario.

    This function uses an agno agent with a specified `response_model`
    to directly generate a validated Pydantic object.

    Args:
        request: The lab exam request.

    Returns:
        The generated and validated laboratory exams as a LabExamResponse object.

    Raises:
        HTTPException: If the generation or validation by the agent fails.
    """
    logger.info(f"Received request to generate lab exams for: {request.tipologia_paziente}")

    try:
        prompt = create_exam_prompt(request)

        run_response: RunResponse = exam_agent.run(prompt) # type: ignore

        validated_exams = run_response.content
        
        if not isinstance(validated_exams, LabExamResponse):
            raise TypeError(f"Agent returned unexpected type: {type(validated_exams)}")

        logger.info(f"Successfully generated lab exams for {request.tipologia_paziente}")
        return validated_exams

    except Exception as e:
        logger.error(f"An unexpected error occurred during exam generation: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate valid lab exams", "message": str(e)})