# Agent for Medical Report Generation
# Version 1.0 - Medical report generation agent

import json
import logging

from agno.agent import Agent
from fastapi import HTTPException

from models.medical_report_models import MedicalReportRequest, MedicalReportResponse
from utils.common import extract_json_from_response, get_exam_model

# Configure logging
logger = logging.getLogger(__name__)


# Agent Definition
medical_report_agent = Agent(
    name="Medical Report Generator",
    role="An expert radiologist and clinician who generates detailed medical reports for various diagnostic examinations.",
    model=get_exam_model(),    instructions=[
        "Your task is to generate realistic medical reports based on clinical scenarios.",
        "All text content must be in **Italian**.",
        "The reports must be medically accurate and appropriate for the described pathology.",
        "Format the main report as plain text with clear structure and proper medical terminology.",
        "Generate ONLY the medical report content without adding conclusions, recommendations, or additional commentary.",
        "Focus on objective findings and observations without interpretative conclusions.",
        "You must respond ONLY with a valid JSON object that strictly matches the required Pydantic schema.",
    ]
)


def create_medical_report_prompt(request: MedicalReportRequest) -> str:
    """Creates the detailed prompt for the medical report generation agent."""
    exam_type = request.tipologia_esame
    
    return f"""
    Generate a detailed medical report in JSON format for a diagnostic examination.

    CLINICAL CONTEXT:
    - Patient Type: {request.tipologia_paziente}
    - Scenario Description: {request.descrizione_scenario}
    - Examination Type: {exam_type}    INSTRUCTIONS:
    1. Generate a realistic medical report appropriate for the clinical scenario and examination type.
    2. All text content MUST be in ITALIAN.    
    3. The `referto` field should contain ONLY the medical report formatted as plain text with clear structure:
       - Include relevant anatomical details and measurements when applicable
       - Structure the text clearly without HTML tags
       - Focus on objective findings and observations
       - DO NOT include conclusions, recommendations, or interpretative comments
       - DO NOT add summary sections or final remarks
    4. Make the report consistent with the patient's age group ({request.tipologia_paziente}).
    5. Ensure medical terminology is accurate and appropriate for the examination type.

    EXAMINATION-SPECIFIC GUIDELINES:
    - For imaging (X-ray, CT, MRI): Include technical parameters, anatomical structures examined, and specific findings
    - For ECG: Include rhythm, rate, intervals, and any abnormalities
    - For Echo: Include chamber dimensions, valve function, and hemodynamic assessment
    - For endoscopy: Include preparation, procedure details, and mucosal findings
    - For functional tests: Include parameters measured and interpretation

    JSON SCHEMA TO FOLLOW:
    {json.dumps(MedicalReportResponse.model_json_schema(), indent=2)}

    Respond ONLY with the valid JSON object.
    """


def generate_medical_report(request: MedicalReportRequest) -> MedicalReportResponse:
    """
    Generate a medical report for a diagnostic examination.
    
    Args:
        request: The medical report request containing scenario, patient type, and exam type
        
    Returns:
        MedicalReportResponse: The generated medical report
        
    Raises:
        HTTPException: If generation fails
    """
    exam_type = request.tipologia_esame
    logger.info(f"Received request to generate medical report for: {exam_type} - Patient: {request.tipologia_paziente}")
    
    try:
        # Create the prompt for the agent
        prompt = create_medical_report_prompt(request)
        
        # Run the agent to get the response
        agent_response = medical_report_agent.run(prompt)  # type: ignore
        
        # Extract and validate the JSON from the response
        report_dict = extract_json_from_response(agent_response.content)
        
        # Validate the data with the Pydantic model
        validated_report = MedicalReportResponse.model_validate(report_dict)
        
        logger.info(f"Successfully generated medical report for {exam_type}")
        return validated_report
        
    except (ValueError, json.JSONDecodeError) as e:
        logger.error(f"Data validation or extraction error: {e}", exc_info=True)
        raise HTTPException(status_code=422, detail={"error": "Generated content failed validation or parsing.", "details": str(e)})
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate medical report", "message": str(e)})
