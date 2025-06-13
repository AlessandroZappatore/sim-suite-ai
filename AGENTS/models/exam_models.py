# Pydantic models for Medical Exam Generation
# Version 1.1 - Refactored from exam_agent.py

from __future__ import annotations

from typing import List, Optional

from pydantic import BaseModel, Field


class LabTest(BaseModel):
    """Represents a single laboratory test result."""
    nome: str = Field(description="Name of the test in Italian (e.g., 'Emoglobina', 'Piastrine').")
    valore: str = Field(description="The resulting value of the test, as a string to accommodate various formats (e.g., '12.5', '250,000', 'Negativo').")
    unita_misura: Optional[str] = Field(description="The unit of measurement for the test (e.g., 'g/dL', 'x10^3/µL', 'mg/dL').")
    range_riferimento: str = Field(description="The reference range for the test (e.g., '13.5 - 17.5').")
    referto: str = Field(description="A brief textual interpretation of the test result in Italian (e.g., 'Valore nella norma', 'Leggermente aumentato').")


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
    esame_obiettivo: str = Field(description="Objective exam like eyes, neck, chest, abdomen, etc.")
