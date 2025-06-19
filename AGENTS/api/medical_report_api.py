"""FastAPI router for medical report generation endpoints.

This module defines the API routes for generating and managing medical reports.
It provides endpoints for creating a report based on a clinical scenario and
for retrieving the list of supported examination and patient types.

Version: 1.1
"""

import logging
from typing import Dict, List

from fastapi import APIRouter
from agents import generate_medical_report
from models import MedicalReportRequest, MedicalReportResponse

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
    """
    Retrieves the lists of available examination and patient types
    dynamically from the request model.
    """
    exam_type_field = MedicalReportRequest.model_fields['tipologia_esame']
    patient_type_field = MedicalReportRequest.model_fields['tipologia_paziente']

    allowed_exam_types = list(exam_type_field.annotation.__args__) if exam_type_field.annotation and hasattr(exam_type_field.annotation, '__args__') else []
    allowed_patient_types = list(patient_type_field.annotation.__args__) if patient_type_field.annotation and hasattr(patient_type_field.annotation, '__args__') else []

    return {
        "exam_types": allowed_exam_types,
        "patient_types": allowed_patient_types
    }
