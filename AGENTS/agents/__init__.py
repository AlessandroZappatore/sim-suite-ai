"""Agents package for the Medical Simulation Suite AI.

This package aggregates the core AI agents and their primary service functions
from the various sub-modules (e.g., scenario, exam, report). It provides a
centralized access point for the main application to use these components.

The `__all__` variable explicitly defines the public API of this package.
"""

from .exam_agents import exam_agent, generate_lab_exams
from .medical_report import medical_report_agent, generate_medical_report
from .scenario_agents import medical_team, info_agent, timeline_agent, script_agent

__all__ = [
    # Scenario agents
    "medical_team", "info_agent", "timeline_agent", "script_agent",
    # Exam agents
    "exam_agent", "generate_lab_exams",
    # Medical report agents
    "medical_report_agent", "generate_medical_report"
]
