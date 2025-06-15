"""API package for the Medical Simulation Suite AI.

This package aggregates all the FastAPI routers from the various sub-modules,
making them available for inclusion in the main application. The `__all__`
variable explicitly defines the public API of this package.
"""

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
