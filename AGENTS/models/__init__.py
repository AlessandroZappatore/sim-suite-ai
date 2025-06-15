"""Data models for the Medical Simulation Suite AI.

This package centralizes all Pydantic data models used for structuring
data within the application. It aggregates models from various sub-packages,
including those for scenarios, medical exams, and reports, providing a single,
consistent access point.

The `__all__` variable explicitly defines the public API of this package,
making it clear which models are intended for external use.
"""

from .exam_models import (
    LabCategory, LabExamRequest, LabExamResponse, LabTest
)
from .medical_report_models import (
    MedicalReportRequest, MedicalReportResponse
)
from .scenario_models import (
    AccessoArterioso, AccessoVenosso, BaseScenario, EsameFisico,
    EsameFisicoSections, FullScenario, ParametroAggiuntivo, PazienteT0,
    Sceneggiatura, ScenarioInfo, ScenarioRequest, Tempo, Timeline
)

__all__ = [
    # Scenario models
    "AccessoArterioso", "AccessoVenosso", "BaseScenario", "EsameFisico",
    "EsameFisicoSections", "FullScenario", "ParametroAggiuntivo", "PazienteT0",
    "Sceneggiatura", "ScenarioInfo", "ScenarioRequest", "Tempo", "Timeline",
    # Exam models
    "LabCategory", "LabExamRequest", "LabExamResponse", "LabTest",
    # Medical report models
    "MedicalReportRequest", "MedicalReportResponse"
]
