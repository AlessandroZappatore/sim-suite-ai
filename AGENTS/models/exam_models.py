"""Pydantic models for medical exam generation.

This module defines the data structures used for requesting and representing
laboratory exam results. The models ensure that data passed to and from the
exam generation service is well-structured and validated.

Version: 1.1
"""
from __future__ import annotations

from typing import List, Optional, Literal

from pydantic import BaseModel, Field


class LabTest(BaseModel):
    """Represents a single laboratory test result.

    Attributes:
        nome: The name of the test in Italian (e.g., 'Emoglobina').
        valore: The resulting value of the test, formatted as a string.
        unita_misura: The unit of measurement for the test (e.g., 'g/dL').
        range_riferimento: The reference range for the test (e.g., '13.5 - 17.5').
        referto: A brief textual interpretation of the result in Italian.
    """
    nome: str = Field(description="Name of the test in Italian (e.g., 'Emoglobina', 'Piastrine').")
    valore: str = Field(description="The resulting value of the test, as a string to accommodate various formats (e.g., '12.5', '250,000', 'Negativo').")
    unita_misura: Optional[str] = Field(description="The unit of measurement for the test (e.g., 'g/dL', 'x10^3/µL', 'mg/dL').")
    range_riferimento: str = Field(description="The reference range for the test (e.g., '13.5 - 17.5').")
    referto: str = Field(description="A brief textual interpretation of the test result in Italian (e.g., 'Valore nella norma', 'Leggermente aumentato').")


class LabCategory(BaseModel):
    """Represents a category of laboratory exams.

    Attributes:
        categoria: The name of the lab category in Italian (e.g., 'Ematologia').
        test: A list of individual lab tests belonging to this category.
    """
    categoria: str = Field(description="The name of the lab category in Italian (e.g., 'Ematologia', 'Chimica Clinica').")
    test: List[LabTest]


class LabExamResponse(BaseModel):
    """Defines the structure for the final lab exams response.

    Attributes:
        esami_laboratorio: A list of laboratory exam categories.
    """
    esami_laboratorio: List[LabCategory]


class LabExamRequest(BaseModel):
    """Defines the request model for generating lab exams.

    Attributes:
        descrizione_scenario: A detailed description of the clinical scenario.
        tipologia_paziente: The type of patient to adjust reference ranges for.
        esame_obiettivo: The objective physical examination findings.
    """
    descrizione_scenario: str = Field(description="A detailed description of the clinical scenario, including patient status and pathology.")
    tipologia_paziente: Literal["Adulto", "Pediatrico", "Neonatale", "Prematuro"] = Field(description="Type of patient (Adult, Pediatric, Neonatal, Premature).")
    esame_obiettivo: str = Field(description="Objective exam like eyes, neck, chest, abdomen, etc.")
    patologia: Optional[str] = Field(default=None, description="Optional pathology to focus the lab tests on (e.g., 'Anemia', 'Diabete').")