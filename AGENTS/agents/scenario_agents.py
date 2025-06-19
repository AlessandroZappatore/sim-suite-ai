"""Defines AI agents and orchestration for medical scenario generation.

This module contains the core components for the AI-driven scenario creation
process. It defines specialist agents for different parts of a scenario
(initial info, timeline, script), functions to generate detailed prompts, and
a MedicalScenarioTeam class to orchestrate the generation pipeline using
agno's structured output capabilities.

Version: 5.0.1 
"""

import logging
from typing import List

from agno.agent import Agent, RunResponse
from fastapi import HTTPException
from pydantic import ValidationError

from models import (
    BaseScenario,
    FullScenario,
    ScenarioRequest,
    Timeline,
    Sceneggiatura,
)
from config import PRESIDI_MEDICI
from utils import get_big_model, get_knowledge_base

# Logger instance
logger = logging.getLogger(__name__)


# --- Agent Definitions ---
info_agent = Agent(
    name="Scenario Info Generator",
    role="An expert in creating the foundational elements of a medical simulation.",
    knowledge=get_knowledge_base(),
    model=get_big_model(),
    response_model=BaseScenario,
    instructions=[
        "Your task is to generate the static, initial part of a medical scenario.",
        "All text content must be in Italian.",
        "Generate specific, plausible data for all fields based on the user's request.",
    ]
)

timeline_agent = Agent(
    name="Clinical Timeline Generator",
    role="An expert in creating dynamic, evolving clinical timelines for medical simulations.",
    model=get_big_model(),
    knowledge=get_knowledge_base(),
    response_model=Timeline,
    instructions=[
        "Your task is to generate a series of timeline events ('tempi').",
        "All text content must be in Italian.",
        "Based on the provided initial scenario context, create a realistic clinical evolution.",
        "Start from T0 reflecting the initial parameters, then evolve the scenario with 4-5 events.",
        "If the patient is pediatric, include a 'ruoloGenitore' field in each event if necessary.",
    ]
)

script_agent = Agent(
    name="Patient Script Writer",
    role="A creative writer specializing in scripts for simulated patients.",
    model=get_big_model(),
    response_model=Sceneggiatura,
    instructions=[
        "Your task is to write a script ('sceneggiatura') for a simulated patient.",
        "The script must be in Italian and formatted as HTML text (e.g., using <p> tags).",
        "Based on the complete scenario provided, write a script that the patient actor can follow.",
    ]
)


# --- Prompt Functions ---
def create_info_prompt(request: ScenarioRequest) -> str:
    """Creates prompt for the Scenario Info Generator agent."""
    difficulty_guidelines = {
        "Facile": {"complications": "Scenario semplice...", "parameters": "Parametri vitali stabili...", "timeline": "Evoluzione lineare...", "actions": "Azioni cliniche di base..."},
        "Medio": {"complications": "Scenario con complessità moderata...", "parameters": "Parametri vitali con alterazioni moderate...", "timeline": "Evoluzione con qualche imprevisto...", "actions": "Combinazione di azioni di base e avanzate..."},
        "Difficile": {"complications": "Scenario complesso...", "parameters": "Parametri vitali instabili...", "timeline": "Evoluzione rapida...", "actions": "Procedure avanzate..."}
    }
    difficulty = request.difficulty or "Facile"
    guidelines = difficulty_guidelines[difficulty]

    return f"""
    Generate the base part of a medical scenario.
    
    ## USER REQUEST
    - Description: {request.description}
    - Scenario Type: {request.scenario_type}
    - Target Audience: {request.target}
    - Difficulty Level: {difficulty}

    ## INSTRUCTIONS
    1.  **Adapt to Target Audience**: The scenario's complexity, pathology, and actions must be suitable for the specified target audience.
    2.  **Adapt to Difficulty ({difficulty})**:
        - Complications: {guidelines['complications']}
        - Vitals: {guidelines['parameters']}
        - Evolution: {guidelines['timeline']}
        - Actions: {guidelines['actions']}
    3.  **Language**: All text must be in ITALIAN.
    4.  **Formatting**: 
        - Use HTML tags (`<p>`, `<ul>`, `<li>`) for descriptive fields as appropriate.
        - Physical exam sections ('esameFisico.sections') must be in `<p>` tags.
        - For vital signs, use `0` if a value is not applicable. Do not use text.
    5.  **Medical Devices ('presidi')**: Choose ONLY from this list: {PRESIDI_MEDICI}
    6.  **Parent/Guardian Role**: If pediatric, describe the parent's initial state in 'scenario.infoGenitore'.
    """

def create_timeline_prompt(base_scenario: BaseScenario, difficulty: str) -> str:
    """Creates the simplified prompt for the Clinical Timeline Generator agent."""
    difficulty_timeline_rules = {
        "Facile": "Evoluzione graduale e stabile. Tempi lenti (5-10 min).",
        "Medio": "Evoluzione moderatamente dinamica. Tempi normali (2-5 min).",
        "Difficile": "Evoluzione rapida e critica. Tempi rapidi (30 sec - 2 min)."
    }
    context_json = base_scenario.model_dump_json(indent=2)
    
    return f"""
    Given the following initial scenario, generate a clinical timeline.
    
    ## INITIAL CONTEXT
    {context_json}
    
    ## INSTRUCTIONS
    1.  Generate 4-5 timeline events ('tempi'), starting with T0 reflecting the initial state.
    2.  Language: All text must be in ITALIAN.
    3.  **Adapt to Difficulty ({difficulty})**: {difficulty_timeline_rules.get(difficulty, "")}
    4.  **Vitals**: Use `0` for not applicable numeric vitals.
    5.  **Parent/Guardian Role**: If pediatric, describe the parent's reaction at each timeline step in the 'ruoloGenitore' field.
    """

def create_script_prompt(full_context: FullScenario) -> str:
    """Creates the simplified prompt for the Patient Script Writer agent."""
    context_json = full_context.model_dump_json(indent=2)
    return f"""
    Given the complete medical scenario below, write a detailed script for the simulated patient.
    
    ## FULL SCENARIO CONTEXT
    {context_json}
    
    ## INSTRUCTIONS
    1.  Write a script in ITALIAN for the patient actor.
    2.  The script must reflect the patient's symptoms and their evolution throughout the timeline.
    3.  Format the script text with HTML tags (e.g., `<p>`).
    """


class MedicalScenarioTeam:
    """Manages a team of agents to generate a complete medical scenario."""
    def __init__(self, members: List[Agent]):
        self.members = {agent.name: agent for agent in members}

    def _get_agent(self, name: str) -> Agent:
        if name not in self.members:
            raise ValueError(f"Agent '{name}' not found in team members.")
        return self.members[name]

    def run(self, request: ScenarioRequest) -> FullScenario:
        """Executes the full scenario generation pipeline using validated Pydantic objects."""
        try:
            # --- STEP 1: Run Info Agent ---
            logger.info("Team Pipeline: Running 'Scenario Info Generator'...")
            info_prompt = create_info_prompt(request)
            info_response: RunResponse = self._get_agent("Scenario Info Generator").run(info_prompt) # type: ignore
            
            content = info_response.content
            if not isinstance(content, BaseScenario):
                raise TypeError(f"Info agent did not return a valid BaseScenario object, got {type(content).__name__} instead.")
            base_scenario: BaseScenario = content
            
            timeline = Timeline(tempi=[])
            sceneggiatura = Sceneggiatura(sceneggiatura="")

            # --- STEP 2: Run Timeline Agent (conditionally) ---
            if request.scenario_type in ["Advanced Scenario", "Patient Simulated Scenario"]:
                logger.info("Team Pipeline: Running 'Clinical Timeline Generator'...")
                timeline_prompt = create_timeline_prompt(base_scenario, request.difficulty or "Facile")
                timeline_response: RunResponse = self._get_agent("Clinical Timeline Generator").run(timeline_prompt) # type: ignore
                
                content = timeline_response.content
                if not isinstance(content, Timeline):
                    raise TypeError(f"Timeline agent did not return a valid Timeline object, got {type(content).__name__} instead.")
                timeline: Timeline = content

            # --- STEP 3: Run Script Agent (conditionally) ---
            if request.scenario_type == "Patient Simulated Scenario":
                logger.info("Team Pipeline: Running 'Patient Script Writer'...")
                script_context_obj = FullScenario(**base_scenario.model_dump(), tempi=timeline.tempi, sceneggiatura="")
                script_prompt = create_script_prompt(script_context_obj)
                script_response: RunResponse = self._get_agent("Patient Script Writer").run(script_prompt) # type: ignore

                content = script_response.content
                if not isinstance(content, Sceneggiatura):
                    raise TypeError(f"Script agent did not return a valid Sceneggiatura object, got {type(content).__name__} instead.")
                sceneggiatura: Sceneggiatura = content
                
            # --- STEP 4: Final Assembly ---
            logger.info("Team Pipeline: Assembling and validating final scenario...")
            final_scenario = FullScenario(
                **base_scenario.model_dump(),
                tempi=timeline.tempi,
                sceneggiatura=sceneggiatura.sceneggiatura
            )
            return final_scenario

        except (ValidationError, TypeError) as e:
            logger.error(f"Data validation or type error in pipeline: {e}", exc_info=True)
            raise HTTPException(status_code=422, detail={"error": "Generated content failed validation or typing.", "details": str(e)})
        except Exception as e:
            logger.error(f"Unexpected error in pipeline: {e}", exc_info=True)
            raise HTTPException(status_code=500, detail={"error": "Failed to generate scenario", "message": str(e)})


medical_team = MedicalScenarioTeam(members=[info_agent, timeline_agent, script_agent])


def generate_medical_scenario(_request: ScenarioRequest) -> FullScenario:
    """Generates a medical scenario by orchestrating a team of agents."""
    logger.info(f"Received request to generate medical scenario: {_request.scenario_type} for {_request.target}")
    return medical_team.run(_request)