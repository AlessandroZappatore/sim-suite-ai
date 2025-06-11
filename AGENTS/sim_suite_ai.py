# Medical Simulation Suite AI
# Version 4.2 - Fixed Pylance static analysis warnings.

# CORREZIONE 1: Aggiunto per migliorare la risoluzione dei type hints da parte di Pylance.
from __future__ import annotations

import json
import logging
import os
import re
from typing import Any, Dict, List, Literal, Optional

from agno.agent import Agent
from agno.models.google import Gemini
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, ValidationError, field_validator

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load environment variables
load_dotenv()

# Verify API key is loaded
if not os.getenv("GOOGLE_API_KEY"):
    logger.warning("GOOGLE_API_KEY not found in environment variables")

# ==============================================================================
# PYDANTIC MODELS (Struttura invariata)
# ==============================================================================
class AccessoVenosso(BaseModel):
    tipologia: Literal["Periferico", "Centrale", "CVC a breve termine", "CVC tunnellizzato", "PICC", "Midline", "Intraosseo", "PORT", "Dialysis catheter", "Altri"]
    posizione: str
    lato: Literal["DX", "SX"]
    misura: int = Field(description="Gauge size of the venous access (es. 14, 16, 18, etc.) max 26")

class AccessoArterioso(BaseModel):
    tipologia: Literal["Radiale", "Femorale", "Omerale", "Brachiale", "Ascellare", "Pedidia", "Altro"]
    posizione: str
    lato: Literal["DX", "SX"]
    misura: int = Field(description="Gauge size of the arterial access (es. 14, 16, 18, etc.) max 26")

class PazienteT0(BaseModel):
    RR: int = Field(description="Respiratory Rate (atti/min)")
    SpO2: int = Field(description="Oxygen Saturation (%)")
    FiO2: float = Field(default=0, description="Fraction of Inspired Oxygen (%)")
    LitriO2: float = Field(default=0, description="Oxygen flow (L/min)")
    EtCO2: int = Field(default=0, description="End-tidal CO2 (mmHg)")
    Monitor: str = Field(description="Descrizione del monitoraggio (plain text, es. ECG, Saturimetria)")
    accessiVenosi: List[AccessoVenosso] = Field(default_factory=list) # type: ignore
    accessiArteriosi: List[AccessoArterioso] = Field(default_factory=list) # type: ignore
    PA: str = Field(description="Blood Pressure (es. '120/80')")
    FC: int = Field(description="Heart Rate (bpm)")
    T: float = Field(description="Temperature (Celsius)")

    @field_validator('PA')
    @classmethod
    def validate_blood_pressure(cls, v: str) -> str:
        if not v or not v.strip(): return "0/0"
        if re.match(r'^\d+/\d+$', v.strip()): return v.strip()
        return "0/0"

class EsameFisicoSections(BaseModel):
    Generale: str = Field(description="General examination findings (html format)")
    Pupille: str = Field(description="Pupil examination findings (html format)") 
    Cute: str = Field(description="Skin examination findings (html format)")
    Collo: str = Field(description="Neck examination findings (html format)")
    Torace: str = Field(description="Chest examination findings (html format)")
    Cuore: str = Field(description="Heart examination findings (html format)")
    Addome: str = Field(description="Abdomen examination findings (html format)")
    Estremità: str = Field(description="Extremities examination findings (html format)")
    Neurologico: str = Field(description="Neurological examination findings (html format)")
    Retto: str = Field(description="Rectal examination findings (html format)")
    FAST: str = Field(description="Focused Assessment with Sonography for Trauma (FAST) findings (html format)")

class EsameFisico(BaseModel):
    sections: EsameFisicoSections

class ScenarioInfo(BaseModel):
    nome_paziente: str = Field(description="Invented or given patient name (plain text)")
    patto_aula: str = Field(description="Classroom agreement (html format)") 
    obiettivo: str = Field(description="Learning objective (html format)") 
    timer_generale: float = Field(description="General timer for the scenario in minutes")
    infoGenitore: Optional[str] = Field(default=None, description="Information given by the parent about the situation (html format)")
    titolo: str = Field(description="Scenario title (plain text)")
    patologia: str = Field(description="Patologia principale (plain text)")
    descrizione: str = Field(description="Scenario description (html format)")
    briefing: str = Field(description="Briefing for the scenario (html format)")
    moulage: str = Field(description="Moulage description (html format)")
    liquidi: str = Field(description="Fluids and drugs description (html format)")
    autori: str = Field(description="Authors of the scenario (plain text) if not given, use 'AI generated'")
    tipologia: Literal["Adulto", "Pediatrico", "Neonatale", "Prematuro"]
    target: str = Field(description="Target given or invented (plain text)")

class ParametroAggiuntivo(BaseModel):
    unitaMisura: str; nome: str; valore: str

class Tempo(BaseModel):
    idTempo: int = Field(description="Unique identifier for the time event (ex. 0 for T0, 1 for T1, etc.)")
    RR: int = Field(description="Respiratory Rate (atti/min)")
    SpO2: int = Field(description="Oxygen Saturation (%)")
    FiO2: float = Field(default=0, description="Fraction of Inspired Oxygen (%)")
    LitriO2: float = Field(default=0, description="Oxygen flow (L/min)")
    EtCO2: int = Field(default=0, description="End-tidal CO2 (mmHg)")
    TSi: int = Field(default=0, description="Time identifier to go if the action is correct (0 for T0, 1 for T1, etc.)")
    TNo: int = Field(default=0, description="Time identifier to go if the action is not correct (0 for T0, 1 for T1, etc.)")
    altriDettagli: str = Field(description="Additional details about the time event (plain text)")
    timerTempo: int = Field(default=0, description="Timer for the time event in seconds")
    ruoloGenitore: Optional[str] = Field(default=None, description="Role of the parent in the time (plain text, if pediatric)")
    PA: str = Field(description="Blood Pressure (sistolic/diastolic, es. '120/80')")
    FC: int = Field(description="Heart Rate (bpm)")
    T: float = Field(description="Temperature (Celsius)")
    Azione: str = Field(description="Action to be performed at this time to go to TSi (plain text)")
    parametriAggiuntivi: List[ParametroAggiuntivo] = Field(default_factory=list) # type: ignore

    @field_validator('PA')
    @classmethod
    def validate_blood_pressure(cls, v: str) -> str:
        if not v or not v.strip(): return "0/0"
        if re.match(r'^\d+/\d+$', v.strip()): return v.strip()
        return "0/0"

class BaseScenario(BaseModel):
    azioniChiave: List[str] 
    tipo: Literal["Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario"]
    scenario: ScenarioInfo
    presidi: List[str]
    esameFisico: EsameFisico
    pazienteT0: PazienteT0

class Timeline(BaseModel):
    tempi: List[Tempo]

class Sceneggiatura(BaseModel):
    sceneggiatura: str = Field(description="Script for the simulated patient (html format)")

class FullScenario(BaseModel):
    azioniChiave: List[str]
    tipo: Literal["Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario"]
    scenario: ScenarioInfo
    presidi: List[str]
    esameFisico: EsameFisico
    pazienteT0: PazienteT0
    tempi: List[Tempo]
    sceneggiatura: str = Field(description="Script for the simulated patient (html format)")

class ScenarioRequest(BaseModel):
    description: str 
    scenario_type: Literal["Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario"]
    target: Optional[str] = None

# ==============================================================================
# UTILITY FUNCTIONS & AGENT DEFINITIONS
# ==============================================================================
def get_model():
    return Gemini("gemini-2.0-flash")

def extract_json_from_response(response_text: Optional[str]) -> Dict[str, Any]:
    if not response_text: raise ValueError("Empty response from AI")
    match = re.search(r'```json\s*(\{.*?\})\s*```', response_text, re.DOTALL)
    json_str = match.group(1) if match else response_text
    try:
        first_brace = json_str.find('{')
        last_brace = json_str.rfind('}')
        return json.loads(json_str[first_brace:last_brace + 1])
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid JSON in AI response: {e}\nResponse text: {json_str}")

info_agent = Agent(name="Scenario Info Generator", 
                   role="An expert in creating the foundational elements of a medical simulation.", 
                   model=get_model(), 
                   instructions=["Your task is to generate the static, initial part of a medical scenario. All text content must be in Italian. Based on the user's request, you must create a JSON object that strictly matches the required Pydantic schema. Respond ONLY with the valid JSON object."])
timeline_agent = Agent(name="Clinical Timeline Generator", 
                       role="An expert in creating dynamic, evolving clinical timelines for medical simulations.", 
                       model=get_model(), 
                       instructions=["Your task is to generate a series of timeline events ('tempi'). All text content must be in Italian. Based on the provided initial scenario context, create a realistic clinical evolution. You must respond ONLY with a valid JSON object strictly matching the Pydantic schema. You have to start from T0 with the same parameters as pazienteT0 and then evolve the scenario with 4-5 events. If the patient is pediatric, include a 'ruoloGenitore' field in each event if necessary."])
script_agent = Agent(name="Patient Script Writer", 
                     role="A creative writer specializing in scripts for simulated patients.", 
                     model=get_model(), 
                     instructions=["Your task is to write a script ('sceneggiatura') for a simulated patient. The script must be in Italian and enclosed in <p></p> tags. Based on the complete scenario provided, write a script that the patient actor can follow. Respond ONLY with a valid JSON object strictly matching the Pydantic schema."])
# ==============================================================================
# PIPELINE TEAM ORCHESTRATOR
# ==============================================================================

class MedicalScenarioTeam:
    def __init__(self, members: List[Agent]):
        self.members = {agent.name: agent for agent in members}

    def _get_agent(self, name: str) -> Agent:
        if name not in self.members:
            raise ValueError(f"Agent '{name}' not found in team members.")
        return self.members[name]

    def _create_info_prompt(self, request: ScenarioRequest) -> str:
        from presidi_medici import PRESIDI_MEDICI
        return f"""Generate the base part of a medical scenario in JSON format.
        USER REQUEST:
        - Description: {request.description}
        - Scenario Type: {request.scenario_type}
        - Target Audience: {request.target}

        INSTRUCTIONS:
        1.  **CRITICAL**: You MUST create and adapt the ENTIRE scenario (pathology, complexity, key actions, objectives) for the specified **Target Audience**. A scenario for "Studenti di infermieristica" must be simpler and more focused on basics than one for "Medici di emergenza esperti".
        2.  The `scenario.target` field in the final JSON MUST match the 'Target Audience' from the user request.
        3.  Strictly follow the JSON schema provided below.
        4.  All descriptive text MUST be in ITALIAN.
        5.  FORMATTING RULES:
            - The fields inside 'esameFisico.sections' (like Generale, Cute, etc.) MUST be formatted as HTML paragraphs, e.g., '<p>text in italian</p>'.
            - The fields 'patologia', 'target', 'autori', and 'Monitor' MUST be plain text without any HTML tags.
            - All other descriptive fields like 'descrizione', 'briefing', 'moulage', 'liquidi', 'obiettivo' etc. must be HTML formatted text.
        6.  For the 'presidi' field, choose ONLY from this list: {PRESIDI_MEDICI}
        7.  Based on the primary pathology ('patologia') and the user's description, you MUST identify and list some crucial medical actions in the 'azioniChiave' field. These actions must be appropriate for the target audience.
        8.  **Vascular Access**: If the clinical context suggests it (e.g., trauma, shock), you MUST populate 'pazienteT0.accessiVenosi' and/or 'pazienteT0.accessiArteriosi'.
        9.  **Parent/Guardian Role**: If 'tipologia' is 'Pediatrico', 'Neonatale', or 'Prematuro', provide context in 'scenario.infoGenitore', describing the parent at the start of the scenario.

        JSON SCHEMA TO FOLLOW:
        {json.dumps(BaseScenario.model_json_schema(), indent=2)}

        Respond ONLY with the valid JSON object."""

    def _create_timeline_prompt(self, context: Dict[str, Any]) -> str:
        return f"""Given the following medical scenario context, generate a clinical timeline.
        CONTEXT: {json.dumps(context, indent=2)}
        
        INSTRUCTIONS:
        1. Generate a list of 4-5 timeline events ('tempi'), starting from T0 which must reflect the initial state.
        2. All descriptive text MUST be in ITALIAN.
        3. **Parent/Guardian Role Explanation**: If the patient is pediatric ('Pediatrico'), the 'ruoloGenitore' field in each 'Tempo' MUST be used to describe the parent's or guardian's actions, words, or emotional state during that specific phase of the timeline. This shows their reaction to the patient's evolving condition.
           - Example for T1: '<p>La madre diventa sempre più agitata, chiede continuamente se il bambino morirà e interferisce con le manovre del team.</p>'
           - Example for T2: '<p>Dopo la somministrazione del farmaco, il padre nota il miglioramento e appare sollevato. Ringrazia i medici.</p>'
           If the parent's role is not relevant for a specific step, you can omit the field or leave it as null.
        4. Strictly follow the JSON schema provided.
        
        JSON SCHEMA TO FOLLOW:
        {json.dumps(Timeline.model_json_schema(), indent=2)}
        
        Respond ONLY with the valid JSON object."""

    def _create_script_prompt(self, context: Dict[str, Any]) -> str:
        return f"""Given the following complete medical scenario, write a script for the simulated patient.
        FULL SCENARIO CONTEXT: {json.dumps(context, indent=2)}
        INSTRUCTIONS:
        1. Write a detailed script in ITALIAN for the patient actor.
        2. The script must reflect the pathology, symptoms, and evolution.
        3. Enclose the entire script in a single <p> HTML tag.
        4. Strictly follow the JSON schema.
        JSON SCHEMA TO FOLLOW: {json.dumps(Sceneggiatura.model_json_schema(), indent=2)}
        Respond ONLY with the valid JSON object."""

    def run(self, request: ScenarioRequest) -> FullScenario:
        try:
            # STEP 1: Run Info Agent
            logger.info("Team Pipeline: Running 'Scenario Info Generator'...")
            info_prompt = self._create_info_prompt(request)
            info_response = self._get_agent("Scenario Info Generator").run(info_prompt) # type: ignore
            base_scenario_dict = extract_json_from_response(info_response.content)
            BaseScenario.model_validate(base_scenario_dict)
            
            full_scenario_data = base_scenario_dict.copy()
            full_scenario_data["tempi"] = []
            full_scenario_data["sceneggiatura"] = "" 
    
            # STEP 2: Run Timeline Agent (if needed)
            if request.scenario_type in ["Advanced Scenario", "Patient Simulated Scenario"]:
                logger.info("Team Pipeline: Running 'Clinical Timeline Generator'...")
                context = {"pathology": base_scenario_dict["scenario"]["patologia"], "patient_t0": base_scenario_dict["pazienteT0"], "patient_typology": base_scenario_dict["scenario"]["tipologia"]}
                timeline_prompt = self._create_timeline_prompt(context)
                timeline_response = self._get_agent("Clinical Timeline Generator").run(timeline_prompt) # type: ignore
                timeline_dict = extract_json_from_response(timeline_response.content)
                full_scenario_data["tempi"] = Timeline.model_validate(timeline_dict).model_dump()["tempi"]
    
            # STEP 3: Run Script Agent (if needed)
            if request.scenario_type == "Patient Simulated Scenario":
                logger.info("Team Pipeline: Running 'Patient Script Writer'...")
                # Usiamo una copia dei dati per evitare di passare la sceneggiatura vuota all'agente
                script_context = full_scenario_data.copy()
                script_prompt = self._create_script_prompt(script_context)
                script_response = self._get_agent("Patient Script Writer").run(script_prompt) # type: ignore
                script_dict = extract_json_from_response(script_response.content)
                
                # CORREZIONE FINALE: Assegnazione spostata all'interno del blocco if
                full_scenario_data["sceneggiatura"] = script_dict["sceneggiatura"]
    
            # STEP 4: Final Validation
            logger.info("Team Pipeline: Assembling and validating final scenario...")
            return FullScenario.model_validate(full_scenario_data)

        except (ValidationError, ValueError) as e:
            logger.error(f"Data validation or extraction error in pipeline: {e}", exc_info=True)
            raise HTTPException(status_code=422, detail={"error": "Generated content failed validation or parsing.", "details": str(e)})
        except Exception as e:
            logger.error(f"Unexpected error in pipeline: {e}", exc_info=True)
            raise HTTPException(status_code=500, detail={"error": "Failed to generate scenario", "message": str(e)})
# ==============================================================================
# FASTAPI APPLICATION
# ==============================================================================
app = FastAPI(title="Medical Simulation AI Team", description="An AI system using a pipeline team of agents to generate medical simulation scenarios.", version="4.2.0")

medical_team = MedicalScenarioTeam(members=[info_agent, timeline_agent, script_agent])

@app.post("/generate-scenario", response_model=FullScenario, summary="Generate a Full Medical Scenario")
def generate_scenario_endpoint(request: ScenarioRequest):
    logger.info(f"Received request for the Medical Scenario Team: {request.model_dump()}")
    return medical_team.run(request)

# For standalone execution
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("sim_suite_ai:app", host="0.0.0.0", port=8001, reload=True)