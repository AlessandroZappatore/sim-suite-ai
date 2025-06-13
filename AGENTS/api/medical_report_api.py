# FastAPI endpoints for Medical Report Generation
# Version 1.0 - Medical report API

import logging

from fastapi import FastAPI

from agents.medical_report import generate_medical_report
from models.medical_report_models import MedicalReportRequest, MedicalReportResponse

# Configure logging
logger = logging.getLogger(__name__)

# Create FastAPI application
medical_report_app = FastAPI(
    title="Medical Report Generation Agent",
    description="An AI agent that generates detailed medical reports for diagnostic examinations.",
    version="1.0.0"
)


@medical_report_app.post("/generate-medical-report", response_model=MedicalReportResponse, summary="Generate Medical Report for an Examination")
def generate_medical_report_endpoint(request: MedicalReportRequest):
    """
    Receives a scenario description and examination type, then generates a corresponding medical report.
    
    Args:
        request: The medical report request containing scenario description, patient type, and exam type
        
    Returns:
        MedicalReportResponse: The generated medical report with conclusions and recommendations
    """
    return generate_medical_report(request)


@medical_report_app.get("/exam-types", summary="Get Available Examination Types")
def get_exam_types():
    """Get list of available examination types for medical reports."""
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


@medical_report_app.get("/health", summary="Health Check")
def health_check():
    """Health check endpoint for the medical report API."""
    return {"status": "healthy", "service": "Medical Report Generator"}
