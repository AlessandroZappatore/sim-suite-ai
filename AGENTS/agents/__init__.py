# Agents package for Medical Simulation Suite AI

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
