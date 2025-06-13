# Pydantic models for Medical Simulation Scenarios
# Version 4.2 - Refactored from sim_suite_ai.py

from __future__ import annotations

import re
from typing import List, Literal, Optional

from pydantic import BaseModel, Field, field_validator


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
    accessiVenosi: List[AccessoVenosso] = Field(default_factory=list)  # type: ignore
    accessiArteriosi: List[AccessoArterioso] = Field(default_factory=list)  # type: ignore
    PA: str = Field(description="Blood Pressure (es. '120/80')")
    FC: int = Field(description="Heart Rate (bpm)")
    T: float = Field(description="Temperature (Celsius)")

    @field_validator('PA')
    @classmethod
    def validate_blood_pressure(cls, v: str) -> str:
        if not v or not v.strip(): 
            return "0/0"
        if re.match(r'^\d+/\d+$', v.strip()): 
            return v.strip()
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
    unitaMisura: str
    nome: str
    valore: str


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
    parametriAggiuntivi: List[ParametroAggiuntivo] = Field(default_factory=list)  # type: ignore

    @field_validator('PA')
    @classmethod
    def validate_blood_pressure(cls, v: str) -> str:
        if not v or not v.strip(): 
            return "0/0"
        if re.match(r'^\d+/\d+$', v.strip()): 
            return v.strip()
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
