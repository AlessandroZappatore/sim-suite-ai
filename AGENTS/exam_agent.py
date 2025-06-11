# exam_agent.py
# Version 1.1 - Added textual report field for each test.

from __future__ import annotations

import json
import logging
import re
from typing import Any, Dict, List, Optional

from agno.agent import Agent
from agno.models.google import Gemini
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load environment variables
load_dotenv()

# ==============================================================================
# PYDANTIC MODELS
# ==============================================================================

class LabTest(BaseModel):
    """Represents a single laboratory test result."""
    nome: str = Field(description="Name of the test in Italian (e.g., 'Emoglobina', 'Piastrine').")
    valore: str = Field(description="The resulting value of the test, as a string to accommodate various formats (e.g., '12.5', '250,000', 'Negativo').")
    unita_misura: Optional[str] = Field(description="The unit of measurement for the test (e.g., 'g/dL', 'x10^3/µL', 'mg/dL').")
    range_riferimento: str = Field(description="The reference range for the test (e.g., '13.5 - 17.5').")
    referto: str = Field(description="A brief textual interpretation of the test result in Italian (e.g., 'Valore nella norma', 'Leggermente aumentato').") # <-- MODIFICA: Aggiunto campo per il referto testuale.

class LabCategory(BaseModel):
    """Represents a category of laboratory exams."""
    categoria: str = Field(description="The name of the lab category in Italian (e.g., 'Ematologia', 'Chimica Clinica').")
    test: List[LabTest]

class LabExamResponse(BaseModel):
    """The final structure for the lab exams response."""
    esami_laboratorio: List[LabCategory]

class LabExamRequest(BaseModel):
    """The request model for generating lab exams."""
    descrizione_scenario: str = Field(description="A detailed description of the clinical scenario, including patient status and pathology.")
    tipologia_paziente: str = Field(default="Adulto", description="Type of patient to adjust reference ranges (e.g., 'Adulto', 'Pediatrico').")


# ==============================================================================
# UTILITY FUNCTIONS & AGENT DEFINITION
# ==============================================================================

def get_model() -> Gemini:
    """Initializes and returns the Gemini model."""
    return Gemini("gemini-1.5-flash-latest")

def extract_json_from_response(response_text: str | None) -> Dict[str, Any]:
    """Extracts a JSON object from the AI's response text."""
    if not response_text:
        raise ValueError("Empty response from AI")
    # Use a more robust regex to find the JSON block
    match = re.search(r'```json\s*(\{.*?\})\s*```', response_text, re.DOTALL)
    if not match:
        # As a fallback, try to parse the whole string if it looks like JSON
        if response_text.strip().startswith('{'):
            json_str = response_text
        else:
            raise ValueError("No JSON block found in the response.")
    else:
        json_str = match.group(1)
    
    try:
        # Sanitize by finding the first '{' and last '}'
        first_brace = json_str.find('{')
        last_brace = json_str.rfind('}')
        if first_brace == -1 or last_brace == -1:
            raise json.JSONDecodeError("Braces not found", json_str, 0)
        return json.loads(json_str[first_brace:last_brace + 1])
    except json.JSONDecodeError as e:
        logger.error(f"JSON Decode Error: {e}\nResponse text received: {json_str}")
        raise ValueError(f"Invalid JSON in AI response: {e}")

# Agent Definition
exam_agent = Agent(
    name="Lab Exam Generator",
    role="An expert clinical pathologist who generates realistic lab results for medical simulations.",
    model=get_model(),
    instructions=[
        "Your task is to generate a set of relevant laboratory exams based on a clinical scenario.",
        "All text content, including test names, categories, and interpretations, must be in **Italian**.", # <-- MODIFICA: Aggiunto 'interpretations'
        "The results must be plausible for the described pathology.",
        "For each test, provide a brief textual interpretation in the `referto` field.", # <-- MODIFICA: Nuova istruzione per l'agente
        "You must respond ONLY with a valid JSON object that strictly matches the required Pydantic schema.",
    ]
)

# ==============================================================================
# PROMPT CREATION
# ==============================================================================

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
    5.  For each test, you MUST include a `referto` field containing a brief clinical interpretation of the result (e.g., "Valore nella norma", "Valore critico indicativo di infiammazione", "Leggermente diminuito"). # <-- MODIFICA: Istruzione esplicita per il campo 'referto'.
    6.  The `unita_misura` field MUST always be a string. If a test has no unit (e.g., a qualitative result like 'Positivo/Negativo'), use an empty string "" or "N/A".
    7.  Strictly adhere to the JSON schema provided below. Do NOT add any extra text or explanations outside the JSON structure.

    JSON SCHEMA TO FOLLOW:
    {json.dumps(LabExamResponse.model_json_schema(), indent=2)}

    Respond ONLY with the valid JSON object.
    """

# ==============================================================================
# FASTAPI APPLICATION
# ==============================================================================

app = FastAPI(
    title="Lab Exam Generation Agent",
    description="An AI agent that generates laboratory exams for medical simulations.",
    version="1.1.0" # <-- MODIFICA: Versione aggiornata
)

@app.post("/generate-lab-exams", response_model=LabExamResponse, summary="Generate Laboratory Exams for a Scenario")
def generate_exams_endpoint(request: LabExamRequest):
    """
    Receives a scenario description and generates a corresponding set of lab results,
    including a textual report for each test.
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
        
        return validated_exams
        
    except (ValueError, json.JSONDecodeError) as e:
        logger.error(f"Data validation or extraction error: {e}", exc_info=True)
        raise HTTPException(status_code=422, detail={"error": "Generated content failed validation or parsing.", "details": str(e)})
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail={"error": "Failed to generate lab exams", "message": str(e)})

# Health check for the agent
@app.get("/health", summary="Health Check")
def health_check():
    return {"status": "healthy"}