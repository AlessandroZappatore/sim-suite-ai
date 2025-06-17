"""Defines AI agents and orchestration for medical scenario generation.

This module contains the core components for the AI-driven scenario creation
process. It defines specialist agents for different parts of a scenario
(initial info, timeline, script), functions to generate detailed prompts, and
a MedicalScenarioTeam class to orchestrate the generation pipeline.

Version: 4.2.0
"""

import json
import logging
from typing import Any, Dict, List

from agno.agent import Agent
from fastapi import HTTPException
from pydantic import ValidationError

from models.scenario_models import (
    BaseScenario,
    FullScenario,
    ScenarioRequest,
    Timeline,
    Sceneggiatura
)
from models.presidi_medici import PRESIDI_MEDICI
from utils.common import extract_json_from_response, get_model

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# --- Agent Definitions ---
# Defines the specialist agents responsible for each part of the scenario generation.
info_agent = Agent(
    name="Scenario Info Generator",
    role="An expert in creating the foundational elements of a medical simulation.",
    model=get_model(),
    instructions=[
        "Your task is to generate the static, initial part of a medical scenario. ",
        "All text content must be in Italian. ",
        "Based on the user's request, you must create a JSON object that strictly matches the required Pydantic schema. ",
        "Respond ONLY with the valid JSON object."
    ]
)

timeline_agent = Agent(
    name="Clinical Timeline Generator",
    role="An expert in creating dynamic, evolving clinical timelines for medical simulations.",
    model=get_model(),
    instructions=[
        "Your task is to generate a series of timeline events ('tempi'). ",
        "All text content must be in Italian. ",
        "Based on the provided initial scenario context, create a realistic clinical evolution. ",
        "You must respond ONLY with a valid JSON object strictly matching the Pydantic schema. ",
        "You have to start from T0 with the same parameters as pazienteT0 and then evolve the scenario with 4-5 events. ",
        "If the patient is pediatric, include a 'ruoloGenitore' field in each event if necessary."
    ]
)

script_agent = Agent(
    name="Patient Script Writer",
    role="A creative writer specializing in scripts for simulated patients.",
    model=get_model(),
    instructions=[
        "Your task is to write a script ('sceneggiatura') for a simulated patient. ",
        "The script must be in Italian and enclosed in <p></p> tags. ",
        "Based on the complete scenario provided, write a script that the patient actor can follow. ",
        "Respond ONLY with a valid JSON object strictly matching the Pydantic schema."
    ]
)


def create_info_prompt(request: ScenarioRequest) -> str:
    """Creates the detailed prompt for the Scenario Info Generator agent.

    This function constructs a comprehensive prompt that includes user request
    details, difficulty-specific guidelines, formatting rules, and the exact
    JSON schema the agent must follow.

    Args:
        request: The user's request object containing the base parameters
            for the scenario.

    Returns:
        A fully formatted prompt string to be sent to the info_agent.
    """
    difficulty_guidelines = {
        "Facile": {
            "complications": "Scenario semplice con poche complicazioni. Concentrarsi sui fondamentali.",
            "parameters": "Parametri vitali stabili o con alterazioni lievi.",
            "timeline": "Evoluzione lineare e prevedibile.",
            "actions": "Azioni cliniche di base e procedure standard."
        },
        "Medio": {
            "complications": "Scenario con complessità moderata. Includere 1-2 complicazioni gestibili.",
            "parameters": "Parametri vitali con alterazioni moderate che richiedono intervento.",
            "timeline": "Evoluzione con qualche imprevisto ma gestibile.",
            "actions": "Combinazione di azioni di base e avanzate. Richiede pensiero critico."
        },
        "Difficile": {
            "complications": "Scenario complesso con multiple complicazioni. Includere situazioni critiche e imprevisti.",
            "parameters": "Parametri vitali instabili o critici. Possibili arresti o shock.",
            "timeline": "Evoluzione rapida e imprevedibile con deterioramento clinico.",
            "actions": "Procedure avanzate, farmaci complessi, decisioni critiche sotto pressione."
        }
    }

    difficulty = request.difficulty or "Facile"
    guidelines = difficulty_guidelines[difficulty]

    return f"""Generate the base part of a medical scenario in JSON format.
    USER REQUEST:
    - Description: {request.description}
    - Scenario Type: {request.scenario_type}
    - Target Audience: {request.target}
    - Difficulty Level: {difficulty}

    INSTRUCTIONS:
    1.  **CRITICAL**: You MUST create and adapt the ENTIRE scenario (pathology, complexity, key actions, objectives) for the specified **Target Audience**. A scenario for "Studenti di infermieristica" must be simpler and more focused on basics than one for "Medici di emergenza esperti".
    2.  **DIFFICULTY ADAPTATION** - {difficulty}:
        - Complicazioni: {guidelines['complications']}
        - Parametri Vitali: {guidelines['parameters']}
        - Evoluzione: {guidelines['timeline']}
        - Azioni Richieste: {guidelines['actions']}
    3.  The `scenario.target` field in the final JSON MUST match the 'Target Audience' from the user request.
    4.  Strictly follow the JSON schema provided below.
    5.  All descriptive text MUST be in ITALIAN.
    6.  FORMATTING RULES:
        - The fields inside 'esameFisico.sections' (like Generale, Cute, etc.) MUST be formatted as HTML paragraphs, e.g., '<p>text in italian</p>'.
        - The fields 'patologia', 'target', 'autori', and 'Monitor' MUST be plain text without any HTML tags.
        - All other descriptive fields like 'descrizione', 'briefing', 'moulage', 'liquidi', 'obiettivo' etc. must be HTML formatted text.
        
        - **FOR LISTS**: When generating content that represents a list (e.g., in the 'liquidi' or 'moulage' fields), you MUST use proper HTML list tags. Use a `<ul>` tag to enclose the list and `<li>` tags for each item.
          - GOOD EXAMPLE: `<ul><li>Soluzione fisiologica.</li><li>Glucosata al 5%.</li><li>Midazolam.</li></ul>`
          - BAD EXAMPLE (DO NOT USE): `Avrete a disposizione:<br><br>Soluzione fisiologica.`
    7.  **NUMERIC PARAMETERS**: For all numeric fields representing vital signs (e.g., RR, SpO2, FiO2, LitriO2, EtCO2, T, FC), you MUST provide a valid number. If a parameter is not measured, not applicable, or unknown, you MUST use the numeric value `0`. **DO NOT use text strings like 'Non misurata' or 'N/A'.**
    8.  For the 'presidi' field, choose ONLY from this list: {PRESIDI_MEDICI}
    9.  Based on the primary pathology ('patologia') and the user's description, you MUST identify and list some crucial medical actions in the 'azioniChiave' field. These actions must be appropriate for the target audience.
    10. **Vascular Access**: If the clinical context suggests it (e.g., trauma, shock), you MUST populate 'pazienteT0.accessiVenosi' and/or 'pazienteT0.accessiArteriosi'.
    11. **Parent/Guardian Role**: If 'tipologia' is 'Pediatrico', 'Neonatale', or 'Prematuro', provide context in 'scenario.infoGenitore', describing the parent at the start of the scenario.

    JSON SCHEMA TO FOLLOW:
    {json.dumps(BaseScenario.model_json_schema(), indent=2)}

    Respond ONLY with the valid JSON object."""


def create_timeline_prompt(context: Dict[str, Any]) -> str:
    """Creates the detailed prompt for the Clinical Timeline Generator agent.

    This function uses the initial scenario context to build a prompt asking
    for a realistic clinical evolution that respects the difficulty level.

    Args:
        context: A dictionary containing foundational scenario elements,
            such as pathology and initial patient parameters.

    Returns:
        A fully formatted prompt string for the timeline_agent.
    """
    difficulty = context.get('difficulty', 'Facile')
    difficulty_timeline_rules = {
        "Facile": "Evoluzione graduale e stabile. Massimo 1 complicazione minore. Tempi di reazione lenti (5-10 minuti tra eventi).",
        "Medio": "Evoluzione moderatamente dinamica. 1-2 complicazioni gestibili. Tempi di reazione normali (2-5 minuti tra eventi).",
        "Difficile": "Evoluzione rapida e critica. Multiple complicazioni simultanee. Deterioramento rapido se non trattato. Tempi di reazione rapidi (30 secondi - 2 minuti)."
    }

    return f"""Given the following medical scenario context, generate a clinical timeline.
    CONTEXT: {json.dumps(context, indent=2)}
    
    INSTRUCTIONS:
    1. Generate a list of 4-5 timeline events ('tempi'), starting from T0 which must reflect the initial state.
    2. All descriptive text MUST be in ITALIAN.
    3. **DIFFICULTY ADAPTATION - {difficulty}**: {difficulty_timeline_rules[difficulty]}
    4. **NUMERIC PARAMETERS**: For all numeric fields representing vital signs (e.g., RR, SpO2, FiO2, LitriO2, EtCO2, T, FC), you MUST provide a valid number. If a parameter is not measured or not applicable, you MUST use the numeric value `0`. **DO NOT use text strings like 'Non misurata' or 'N/A'.**
    5. **Parent/Guardian Role Explanation**: If the patient is pediatric ('Pediatrico'), the 'ruoloGenitore' field in each 'Tempo' MUST be used to describe the parent's or guardian's actions, words, or emotional state during that specific phase of the timeline. This shows their reaction to the patient's evolving condition.
        - Example for T1: '<p>La madre diventa sempre più agitata, chiede continuamente se il bambino morirà e interferisce con le manovre del team.</p>'
        - Example for T2: '<p>Dopo la somministrazione del farmaco, il padre nota il miglioramento e appare sollevato. Ringrazia i medici.</p>'
        If the parent's role is not relevant for a specific step, you can omit the field or leave it as null.
    6. Strictly follow the JSON schema provided.
    
    JSON SCHEMA TO FOLLOW:
    {json.dumps(Timeline.model_json_schema(), indent=2)}
    
    Respond ONLY with the valid JSON object."""


def create_script_prompt(context: Dict[str, Any]) -> str:
    """Creates the detailed prompt for the Patient Script Writer agent.

    Args:
        context: A dictionary containing the complete scenario data,
            including the timeline, to inform the script.

    Returns:
        A fully formatted prompt string for the script_agent.
    """
    return f"""Given the following complete medical scenario, write a script for the simulated patient.
    FULL SCENARIO CONTEXT: {json.dumps(context, indent=2)}
    INSTRUCTIONS:
    1. Write a detailed script in ITALIAN for the patient actor.
    2. The script must reflect the pathology, symptoms, and evolution.
    3. Enclose the entire script in a single <p> HTML tag.
    4. Strictly follow the JSON schema.
    JSON SCHEMA TO FOLLOW: {json.dumps(Sceneggiatura.model_json_schema(), indent=2)}
    Respond ONLY with the valid JSON object."""


class MedicalScenarioTeam:
    """Manages a team of agents to generate a complete medical scenario.

    This class orchestrates a multi-step pipeline where each agent contributes
    its specialty in sequence to build a rich, detailed, and validated
    medical simulation scenario.

    Attributes:
        members: A dictionary mapping agent names to agent instances.
    """

    def __init__(self, members: List[Agent]):
        """Initializes the MedicalScenarioTeam.

        Args:
            members: A list of Agent instances that form the team.
        """
        self.members = {agent.name: agent for agent in members}

    def _get_agent(self, name: str) -> Agent:
        """Retrieves a team member by name.

        Args:
            name: The name of the agent to retrieve.

        Returns:
            The requested agent instance.

        Raises:
            ValueError: If an agent with the specified name is not found.
        """
        if name not in self.members:
            raise ValueError(f"Agent '{name}' not found in team members.")
        return self.members[name]

    def run(self, request: ScenarioRequest) -> FullScenario:
        """Executes the full scenario generation pipeline.

        The pipeline proceeds in the following order:
        1.  The `Scenario Info Generator` creates the base scenario.
        2.  The `Clinical Timeline Generator` adds the evolving timeline.
        3.  The `Patient Script Writer` creates the patient's script.
        4.  The final result is validated against the FullScenario model.

        Args:
            request: The initial request from the user.

        Returns:
            The complete and validated medical scenario.

        Raises:
            HTTPException: If any step of the generation, validation, or
                parsing fails.
        """
        try:
            # STEP 1: Run Info Agent
            logger.info("Team Pipeline: Running 'Scenario Info Generator'...")
            info_prompt = create_info_prompt(request)
            info_response = self._get_agent("Scenario Info Generator").run(info_prompt) # type: ignore
            base_scenario_dict = extract_json_from_response(info_response.content)
            BaseScenario.model_validate(base_scenario_dict)

            full_scenario_data = base_scenario_dict.copy()
            full_scenario_data["tempi"] = []
            full_scenario_data["sceneggiatura"] = ""

            # STEP 2: Run Timeline Agent (if needed)
            if request.scenario_type in ["Advanced Scenario", "Patient Simulated Scenario"]:
                logger.info("Team Pipeline: Running 'Clinical Timeline Generator'...")
                context: Dict[str, Any] = {
                    "pathology": base_scenario_dict["scenario"]["patologia"],
                    "patient_t0": base_scenario_dict["pazienteT0"],
                    "patient_typology": base_scenario_dict["scenario"]["tipologia"],
                    "difficulty": request.difficulty
                }
                timeline_prompt = create_timeline_prompt(context)
                timeline_response = self._get_agent("Clinical Timeline Generator").run(timeline_prompt) # type: ignore
                timeline_dict = extract_json_from_response(timeline_response.content)
                full_scenario_data["tempi"] = Timeline.model_validate(timeline_dict).model_dump()["tempi"]

            # STEP 3: Run Script Agent (if needed)
            if request.scenario_type == "Patient Simulated Scenario":
                logger.info("Team Pipeline: Running 'Patient Script Writer'...")
                script_context = full_scenario_data.copy()
                script_prompt = create_script_prompt(script_context)
                script_response = self._get_agent("Patient Script Writer").run(script_prompt) # type: ignore
                script_dict = extract_json_from_response(script_response.content)
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


# --- Team Instantiation ---
medical_team = MedicalScenarioTeam(members=[info_agent, timeline_agent, script_agent])


def generate_medical_scenario(request: ScenarioRequest) -> FullScenario:
    """Generates a medical scenario by orchestrating a team of agents.

    This function serves as the main public entry point for the scenario
    generation service. It delegates the complex generation logic to the
    `medical_team`.

    Args:
        request: The scenario request containing the description, type,
            target audience, and other parameters.

    Returns:
        The complete, generated medical scenario.

    Raises:
        HTTPException: Propagated from the `MedicalScenarioTeam.run` method
            if the generation or validation process fails.
    """
    logger.info(f"Received request to generate medical scenario: {request.scenario_type} for {request.target}")
    return medical_team.run(request)
