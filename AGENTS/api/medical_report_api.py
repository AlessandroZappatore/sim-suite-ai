"""FastAPI router for medical report generation endpoints.

This module defines the API routes for generating and managing medical reports.
It provides endpoints for creating a report based on a clinical scenario and
for retrieving the list of supported examination and patient types.

Version: 1.1
"""

import logging
from typing import Dict, List

from fastapi import APIRouter
from agents.medical_report import generate_medical_report
from models.medical_report_models import MedicalReportRequest, MedicalReportResponse

# Configure logging
logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/reports",
    tags=["Reports"]
)


@router.post("/generate-medical-report", response_model=MedicalReportResponse, summary="Generate Medical Report for an Examination")
def generate_medical_report_endpoint(request: MedicalReportRequest) -> MedicalReportResponse:
    """Generates a medical report for a specific examination.

    This endpoint takes a detailed clinical scenario and generates a formal
    medical report for the specified examination type.

    Args:
        request: A request object containing the scenario description, patient
            type, and the type of examination to report on.

    Returns:
        A response object containing the generated medical report.
    """
    logger.info(f"Received request to generate medical report: {request.model_dump()}")
    return generate_medical_report(request)


@router.get("/exam-types", summary="Get Available Examination Types")
def get_exam_types() -> Dict[str, List[str]]:
    """Retrieves the lists of available examination and patient types.

    This endpoint provides the supported values for creating medical report
    requests, which can be used to populate frontend selection inputs.

    Returns:
        A dictionary containing a list of supported 'exam_types' and
        'patient_types'.
    """
    return {
        "exam_types": [
            "ECG (Elettrocardiogramma)", "RX Torace", "TC Torace (con mdc)", "TC Torace (senza mdc)",
            "TC Addome (con mdc)", "TC Addome (senza mdc)", "Ecografia addominale", "Ecografia polmonare",
            "Ecocardio (Transtoracico)", "Ecocardio (Transesofageo)", "Spirometria", "EEG (Elettroencefalogramma)",
            "RM Encefalo", "TC Cranio (con mdc)", "TC Cranio (senza mdc)", "Doppler TSA (Tronchi Sovraortici)",
            "Angio-TC Polmonare", "Fundus oculi"
        ],
        "patient_types": ["Adulto", "Pediatrico", "Neonatale", "Prematuro"]
    }
