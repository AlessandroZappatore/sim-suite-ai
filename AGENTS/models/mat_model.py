from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field

class MATModelRequest(BaseModel):
    """Request model for MAT (Medical Assessment Tool) generation."""
    descrizione_scenario: str = Field(
        description="A detailed description of the clinical scenario, including patient status and pathology."
    )
    tipologia_paziente: Literal["Adulto", "Pediatrico", "Neonatale", "Prematuro"] = Field(
        description="Type of patient (Adult, Pediatric, Neonatal, Premature)."
    )
    target: str = Field(
        description="Target of scenario, e.g studenti di medicina, infermieri, medici specialisti."
    )
    esame_obiettivo: str = Field(
        description="Detailed objective examination findings or exam type. Can include complete physical exam results to provide context for material selection (e.g., pupil assessment, neurological findings, vital signs, etc.)."
    )

class MATModelResponse(BaseModel):
    """Response model for MAT generation."""
    nome : str = Field(
        description="Name of material."
    )
    descrizione_scenario: str = Field(
        description="Description of the material."
    )