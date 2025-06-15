# API package for Medical Simulation Suite AI

from .exam_api import router as exam_router
from .medical_report_api import router as medical_report_router
from .scenario_api import router as scenario_router
from .mat_api import router as material_router

__all__ = [
    "exam_router",
    "medical_report_router",
    "scenario_router",
    "material_router"
]