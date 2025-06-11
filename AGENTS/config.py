# Configuration file for SimSuite AI agents
# Add new agents here to automatically include them in the main application

from typing import Dict, List
from pydantic import BaseModel

class AgentConfig(BaseModel):
    name: str
    module_name: str
    mount_path: str
    port: int
    description: str
    version: str

# Configuration for all available agents
AGENTS_CONFIG: List[AgentConfig] = [
    AgentConfig(
        name="Medical Simulation",
        module_name="sim_suite_ai",
        mount_path="/medical",
        port=8001,
        description="AI system for generating medical simulation scenarios",
        version="4.2.0"
    ),
    AgentConfig(
        name="Medical exam",
        module_name="exam_agent",
        mount_path="/exam", 
        port=8002,
        description="Specialized AI agent for medical exams",
        version="1.0.0"
    )
]

# Main application configuration
MAIN_CONFIG: Dict[str, str] = {
    "title": "SimSuite AI - Multi-Agent Platform",
    "description": "A comprehensive AI platform with multiple specialized agents",
    "version": "1.0.0",
    "host": "0.0.0.0",
    "port": "8000"
}

def get_agent_config(agent_name: str) -> AgentConfig:
    """Get configuration for a specific agent"""
    for config in AGENTS_CONFIG:
        if config.name == agent_name:
            return config
    raise ValueError(f"Agent {agent_name} not found in configuration")

def get_all_agents() -> List[AgentConfig]:
    """Get all agent configurations"""
    return AGENTS_CONFIG
