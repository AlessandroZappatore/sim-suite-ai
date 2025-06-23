"""Defines the AI agent and logic for medical report generation.

This module contains the core components for the AI-driven medical report
creation process. It leverages agno's structured output to ensure the
generated report is correctly formatted and validated against a Pydantic model.

Version: 2.0 (Refactored to use agno's response_model)
"""

import logging
from agno.agent import Agent, RunResponse
from fastapi import HTTPException

from models import MedicalReportRequest, MedicalReportResponse
from utils import get_small_model, get_report_knowledge_base

logger = logging.getLogger(__name__)


medical_report_agent = Agent(
    name="Medical Report Generator",
    role="An expert radiologist and clinician who generates detailed medical reports for various diagnostic examinations.",
    model=get_small_model(),
    knowledge=get_report_knowledge_base(),
    response_model=MedicalReportResponse,
    instructions=[
        "Your task is to generate realistic medical reports based on clinical scenarios.",
        "You MUST generate specific and plausible data for all fields. DO NOT use placeholders, brackets like `[...]`, or generic text.",
        "All text content must be in **Italian**.",
        "The reports must be medically accurate and appropriate for the described pathology.",
        "Pay particular attention to the objective examination findings (esame_obiettivo) to ensure the report is consistent with clinical observations.",
        "Use the objective examination information to guide the expected findings in the diagnostic report.",
        "Generate ONLY the medical report content. Focus on objective findings and observations without adding separate conclusions or recommendations not part of the standard report structure.",
        "Correlate the diagnostic findings with the clinical examination findings when appropriate.",
    ]
)


def create_medical_report_prompt(request: MedicalReportRequest) -> str:
    """Creates the prompt for the medical report generation agent.

    Args:
        request: The user's request containing the clinical context.

    Returns:
        A fully formatted prompt string for the medical_report_agent.
    """
    return f"""
    Generate a detailed medical report for a diagnostic examination based on the following context.

    ## CLINICAL CONTEXT
    - Patient Type: {request.tipologia_paziente}
    - Scenario Description: {request.descrizione_scenario}
    - Examination Type: {request.tipologia_esame}
    - Objective Examination (Esame obiettivo): {request.esame_obiettivo}

    ## INSTRUCTIONS
    1.  Generate a realistic medical report appropriate for the scenario and examination type.
    2.  Fill all fields with specific, fabricated data as a real clinician would. Do not use placeholders.
    3.  All text must be in Italian.
    4.  The `referto` field should contain only the objective findings and observations, structured clearly as plain text.
    5.  Ensure the findings are consistent with the provided clinical and objective examination details.
    """


def generate_medical_report(request: MedicalReportRequest) -> MedicalReportResponse:
    """Generates and validates a medical report for a diagnostic examination.

    This function uses an agno agent with a specified `response_model`
    to directly generate a validated Pydantic object.

    Args:
        request: The medical report request.

    Returns:
        The generated and validated medical report.

    Raises:
        HTTPException: If the generation or validation by the agent fails.
    """
    exam_type = request.tipologia_esame
    logger.info(f"Received request to generate medical report for: {exam_type} - Patient: {request.tipologia_paziente}")

    try:
        prompt = create_medical_report_prompt(request)

        run_response: RunResponse = medical_report_agent.run(prompt) # type: ignore

        validated_report = run_response.content

        if not isinstance(validated_report, MedicalReportResponse):
            raise TypeError(f"Agent returned unexpected type: {type(validated_report)}. Expected MedicalReportResponse.")

        logger.info(f"Successfully generated medical report for {exam_type}")
        return validated_report

    except Exception as e:
        logger.error(f"An unexpected error occurred during report generation: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate medical report", "message": str(e)})