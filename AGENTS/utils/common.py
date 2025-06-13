# Common utility functions for Medical Simulation Suite AI
# Version 4.2 - Refactored from sim_suite_ai.py and exam_agent.py

import json
import logging
import os
import re
from typing import Any, Dict, Optional

from agno.models.google import Gemini
from dotenv import load_dotenv

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load environment variables
load_dotenv()

# Verify API key is loaded
if not os.getenv("GOOGLE_API_KEY"):
    logger.warning("GOOGLE_API_KEY not found in environment variables")


def get_model() -> Gemini:
    """Initializes and returns the Gemini model for scenario generation."""
    return Gemini("gemini-2.0-flash")


def get_exam_model() -> Gemini:
    """Initializes and returns the Gemini model for exam generation."""
    return Gemini("gemini-1.5-flash-latest")


def extract_json_from_response(response_text: Optional[str]) -> Dict[str, Any]:
    """
    Extracts a JSON object from the AI's response text.
    
    Args:
        response_text: The response text from the AI model
        
    Returns:
        Dict containing the parsed JSON
        
    Raises:
        ValueError: If the response is empty or contains invalid JSON
    """
    if not response_text: 
        raise ValueError("Empty response from AI")
    
    # Try to find JSON within ```json``` blocks first
    match = re.search(r'```json\s*(\{.*?\})\s*```', response_text, re.DOTALL)
    
    if match:
        json_str = match.group(1)
    else:
        # As a fallback, try to parse the whole string if it looks like JSON
        if response_text.strip().startswith('{'):
            json_str = response_text
        else:
            raise ValueError("No JSON block found in the response.")
    
    try:
        # Sanitize by finding the first '{' and last '}'
        first_brace = json_str.find('{')
        last_brace = json_str.rfind('}')
        
        if first_brace == -1 or last_brace == -1:
            raise json.JSONDecodeError("Braces not found", json_str, 0)
            
        parsed_json = json.loads(json_str[first_brace:last_brace + 1])
        return parsed_json
        
    except json.JSONDecodeError as e:
        logger.error(f"JSON Decode Error: {e}\nResponse text received: {json_str}")
        raise ValueError(f"Invalid JSON in AI response: {e}\nResponse text: {json_str}")
