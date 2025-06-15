"""Pydantic models for medical report generation.

This module defines the data structures for requesting and representing
medical reports. These models ensure that data for the report generation
service is well-structured and validated.

Version: 1.0
"""
from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


class MedicalReportRequest(BaseModel):
    """Defines the request model for generating a medical report.

    Attributes:
        descrizione_scenario: A detailed description of the clinical scenario.
        tipologia_paziente: The type of patient (e.g., Adult, Pediatric).
        tipologia_esame: The specific type of medical examination to report on.
        esame_obiettivo: Objective physical examination findings for context.
    """
    descrizione_scenario: str = Field(description="A detailed description of the clinical scenario, including patient status and pathology.")
    tipologia_paziente: Literal["Adulto", "Pediatrico", "Neonatale", "Prematuro"] = Field(description="Type of patient (Adult, Pediatric, Neonatal, Premature).")
    tipologia_esame: Literal[
        "ECG (Elettrocardiogramma)", "RX Torace", "TC Torace (con mdc)", "TC Torace (senza mdc)",
        "TC Addome (con mdc)", "TC Addome (senza mdc)", "Ecografia addominale", "Ecografia polmonare",
        "Ecocardio (Transtoracico)", "Ecocardio (Transesofageo)", "Spirometria", "EEG (Elettroencefalogramma)",
        "RM Encefalo", "TC Cranio (con mdc)", "TC Cranio (senza mdc)", "Doppler TSA (Tronchi Sovraortici)",
        "Angio-TC Polmonare", "Fundus oculi"
    ] = Field(description="Type of medical examination to generate report for.")
    esame_obiettivo: str = Field(description="Objective exam like eyes, neck, chest, abdomen, etc.")


class MedicalReportResponse(BaseModel):
    """Defines the response model for a generated medical report.

    Attributes:
        tipologia_esame: The type of examination that was performed.
        referto: The complete medical report text in Italian.
    """
    tipologia_esame: str = Field(description="The type of examination performed.")
    referto: str = Field(description="The complete medical report in Italian, formatted as plain text.")
