"""Pydantic models for medical simulation scenarios.

This module defines the data structures used throughout the Medical Simulation
Suite for creating, validating, and managing clinical scenarios. The models
cover everything from patient state and physical exams to scenario timelines
and request parameters.

Version: 4.2
"""

from __future__ import annotations

import re
from typing import List, Literal, Optional

from pydantic import BaseModel, Field, field_validator


class AccessoVenosso(BaseModel):
    """Represents a single venous access point on a patient.

    Attributes:
        tipologia: The type of venous access.
        posizione: The anatomical location of the access point.
        lato: The side of the body (DX for right, SX for left).
        misura: The gauge size of the access device (e.g., 18G).
    """
    tipologia: Literal["Periferico", "Centrale", "CVC a breve termine", "CVC tunnellizzato", "PICC", "Midline", "Intraosseo", "PORT", "Dialysis catheter", "Altri"]
    posizione: str
    lato: Literal["DX", "SX"]
    misura: int = Field(description="Gauge size of the venous access (es. 14, 16, 18, etc.) max 26")


class AccessoArterioso(BaseModel):
    """Represents a single arterial access point on a patient.

    Attributes:
        tipologia: The type of arterial access.
        posizione: The anatomical location of the access point.
        lato: The side of the body (DX for right, SX for left).
        misura: The gauge size of the access device (e.g., 20G).
    """
    tipologia: Literal["Radiale", "Femorale", "Omerale", "Brachiale", "Ascellare", "Pedidia", "Altro"]
    posizione: str
    lato: Literal["DX", "SX"]
    misura: int = Field(description="Gauge size of the arterial access (es. 14, 16, 18, etc.) max 26")


class PazienteT0(BaseModel):
    """Defines the initial state and vital signs of the patient at time zero.

    Attributes:
        RR: Respiratory Rate in breaths per minute.
        SpO2: Oxygen saturation in percent.
        FiO2: Fraction of Inspired Oxygen.
        LitriO2: Oxygen flow in liters per minute.
        EtCO2: End-tidal CO2 in mmHg.
        Monitor: Description of the initial patient monitoring setup.
        accessiVenosi: A list of established venous access points.
        accessiArteriosi: A list of established arterial access points.
        PA: Blood pressure, formatted as 'systolic/diastolic'.
        FC: Heart rate in beats per minute.
        T: Body temperature in Celsius.
    """
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
        """Validates that blood pressure is in the format 'number/number'."""
        if v and re.match(r'^\d+/\d+$', v.strip()):
            return v.strip()
        return "0/0"


class EsameFisicoSections(BaseModel):
    """Contains the detailed findings for each section of a physical exam.

    Each attribute holds HTML-formatted text describing the findings for that
    specific part of the examination.
    """
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
    """A container for the structured physical examination findings.

    Attributes:
        sections: An object containing the detailed findings for each
            part of the physical exam.
    """
    sections: EsameFisicoSections


class ScenarioInfo(BaseModel):
    """Holds general information and metadata about the simulation scenario.

    Attributes:
        nome_paziente: The patient's name.
        patto_aula: The "classroom agreement" or rules for the simulation.
        obiettivo: The primary learning objective of the scenario.
        timer_generale: The total duration of the scenario in minutes.
        infoGenitore: Information provided by a parent (for pediatric cases).
        titolo: The title of the scenario.
        patologia: The primary pathology or condition being simulated.
        descrizione: A general description of the scenario.
        briefing: The pre-scenario briefing for participants.
        moulage: Description of the patient manikin's setup and appearance.
        liquidi: Description of available fluids and drugs.
        autori: The authors of the scenario.
        tipologia: The patient type (e.g., Adult, Pediatric).
        target: The target audience for the simulation (e.g., medical students).
    """
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
    """Represents an additional, custom vital sign or parameter.

    Attributes:
        unitaMisura: The unit of measurement for the parameter.
        nome: The name of the parameter.
        valore: The value of the parameter.
    """
    unitaMisura: str
    nome: str
    valore: str


class Tempo(BaseModel):
    """Represents a specific time point or event within the scenario's timeline.

    Attributes:
        idTempo: A unique identifier for the time point (e.g., 0 for T0).
        TSi: The ID of the next time point if the correct action is taken.
        TNo: The ID of the next time point if the incorrect action is taken.
        altriDettagli: Additional details about the state at this time.
        timerTempo: A specific timer for this event in seconds.
        ruoloGenitore: Role or actions of a parent at this time point.
        Azione: The key action required to proceed to the 'TSi' state.
        parametriAggiuntivi: A list of any custom parameters for this time point.
    """
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
        """Validates that blood pressure is in the format 'number/number'."""
        if v and re.match(r'^\d+/\d+$', v.strip()):
            return v.strip()
        return "0/0"


class BaseScenario(BaseModel):
    """Defines the foundational components of a medical scenario.

    Attributes:
        azioniChiave: A list of key actions or learning objectives.
        tipo: The type of scenario (e.g., Quick, Advanced).
        scenario: General information and metadata about the scenario.
        presidi: A list of required medical equipment.
        esameFisico: The structured physical examination findings.
        pazienteT0: The initial state of the patient.
    """
    azioniChiave: List[str]
    tipo: Literal["Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario"]
    scenario: ScenarioInfo
    presidi: List[str]
    esameFisico: EsameFisico
    pazienteT0: PazienteT0


class Timeline(BaseModel):
    """A container for the entire sequence of time events in a scenario.

    Attributes:
        tempi: A list of `Tempo` objects that define the scenario's progression.
    """
    tempi: List[Tempo]


class Sceneggiatura(BaseModel):
    """Contains the script for a simulated patient or actor.

    Attributes:
        sceneggiatura: An HTML-formatted string containing the script.
    """
    sceneggiatura: str = Field(description="Script for the simulated patient (html format)")


class FullScenario(BaseModel):
    """Represents a complete, advanced scenario with all possible components.

    This model combines the base scenario with a timeline and a script for a
    fully interactive simulation.
    """
    azioniChiave: List[str]
    tipo: Literal["Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario"]
    scenario: ScenarioInfo
    presidi: List[str]
    esameFisico: EsameFisico
    pazienteT0: PazienteT0
    tempi: List[Tempo]
    sceneggiatura: str = Field(description="Script for the simulated patient (html format)")


class ScenarioRequest(BaseModel):
    """Defines the request model for generating a new simulation scenario.

    Attributes:
        description: A text description of the desired clinical case.
        scenario_type: The type of scenario to generate.
        target: The intended audience for the scenario.
        difficulty: The desired difficulty level, which influences clinical
            complexity and potential complications.
    """
    description: str
    scenario_type: Literal["Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario"]
    target: Optional[str] = None
    difficulty: Literal["Facile", "Medio", "Difficile"] = Field(
        default="Facile",
        description="Livello di difficoltà dello scenario che influenza la complessità clinica, le complicazioni e i dettagli"
    )
