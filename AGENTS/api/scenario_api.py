# FastAPI endpoints for Medical Scenario Generation
# Version 4.2 - Refactored from sim_suite_ai.py

import logging

from fastapi import FastAPI

from agents.scenario_agents import generate_medical_scenario
from models.scenario_models import FullScenario, ScenarioRequest

# Configure logging
logger = logging.getLogger(__name__)

# Create FastAPI application
scenario_app = FastAPI(
    title="Medical Simulation AI Team", 
    description="An AI system using a pipeline team of agents to generate medical simulation scenarios.", 
    version="4.2.0"
)


@scenario_app.post("/generate-scenario", response_model=FullScenario, summary="Generate a Full Medical Scenario")
def generate_scenario_endpoint(request: ScenarioRequest):
    """
    Generate a complete medical scenario based on the request parameters.
    
    Args:
        request: The scenario request containing description, type, and target audience
        
    Returns:
        FullScenario: The generated medical scenario
    """
    logger.info(f"Received request for the Medical Scenario Team: {request.model_dump()}")
    return generate_medical_scenario(request)


@scenario_app.get("/health", summary="Health Check")
def health_check():
    """Health check endpoint for the scenario API."""
    return {"status": "healthy", "service": "Medical Scenario Generator"}
