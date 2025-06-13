# Pydantic models for Medical Report Generation
# Version 1.0 - Medical report models

from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


class MedicalReportRequest(BaseModel):
    """The request model for generating medical reports."""
    descrizione_scenario: str = Field(description="A detailed description of the clinical scenario, including patient status and pathology.")
    tipologia_paziente: Literal["Adulto", "Pediatrico", "Neonatale", "Prematuro"] = Field(description="Type of patient (Adult, Pediatric, Neonatal, Premature).")
    tipologia_esame: Literal[
        "ECG (Elettrocardiogramma)", "RX Torace", "TC Torace (con mdc)", "TC Torace (senza mdc)",
            "TC Addome (con mdc)", "TC Addome (senza mdc)", "Ecografia addominale", "Ecografia polmonare",
            "Ecocardio (Transtoracico)", "Ecocardio (Transesofageo)", "Spirometria", "EEG (Elettroencefalogramma)",
            "RM Encefalo", "TC Cranio (con mdc)", "TC Cranio (senza mdc)", "Doppler TSA (Tronchi Sovraortici)",
            "Angio-TC Polmonare", "Fundus oculi"
    ] = Field(description="Type of medical examination to generate report for.")


class MedicalReportResponse(BaseModel):
    """The response model containing the generated medical report."""
    tipologia_esame: str = Field(description="The type of examination performed.")
    referto: str = Field(description="The complete medical report in Italian, formatted as plain text.")
