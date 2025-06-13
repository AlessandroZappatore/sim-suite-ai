# API package for Medical Simulation Suite AI

from .exam_api import exam_app
from .medical_report_api import medical_report_app
from .scenario_api import scenario_app

__all__ = [
    "exam_app", "medical_report_app", "scenario_app"
]
