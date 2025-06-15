# Common utility functions for Medical Simulation Suite AI
# Version 4.2 - Refactored from sim_suite_ai.py and exam_agent.py

import json
import logging
import os
import re
from typing import Any, Dict, Optional

from agno.models.google import Gemini
from dotenv import load_dotenv

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

load_dotenv()

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
    match = re.search(r'```json\s*(\[.*?\]|\{.*?\})\s*```', response_text, re.DOTALL)
    
    if match:
        json_str = match.group(1)
    else:
        stripped = response_text.strip()
        if stripped.startswith('{') or stripped.startswith('['):
            json_str = response_text
        else:
            raise ValueError("No JSON block found in the response.")
    
    try:
        json_str = json_str.strip()
        
        parsed_json = json.loads(json_str)
        return parsed_json
        
    except json.JSONDecodeError as e:
        try:
            if json_str.strip().startswith('['):
                first_bracket = json_str.find('[')
                last_bracket = json_str.rfind(']')
                if first_bracket != -1 and last_bracket != -1:
                    clean_json = json_str[first_bracket:last_bracket + 1]
                    parsed_json = json.loads(clean_json)
                    return parsed_json
            
            elif json_str.strip().startswith('{'):
                first_brace = json_str.find('{')
                last_brace = json_str.rfind('}')
                if first_brace != -1 and last_brace != -1:
                    clean_json = json_str[first_brace:last_brace + 1]
                    parsed_json = json.loads(clean_json)
                    return parsed_json
            
            raise e
            
        except json.JSONDecodeError:
            logger.error(f"JSON Decode Error: {e}\nResponse text received: {json_str}")
            raise ValueError(f"Invalid JSON in AI response: {e}\nResponse text: {json_str}")
        
    except Exception as e:
        logger.error(f"Unexpected error parsing JSON: {e}\nResponse text received: {json_str}")
        raise ValueError(f"Unexpected error parsing JSON: {e}\nResponse text: {json_str}")


def sanitize_json_string(json_str: str) -> str:
    """
    Sanitize a JSON string by removing or replacing problematic characters.
    
    Args:
        json_str: The JSON string to sanitize
        
    Returns:
        Cleaned JSON string
    """
    import re
    
    json_str = json_str.replace('"', '"').replace('"', '"')
    json_str = json_str.replace(''', "'").replace(''', "'")
    
    json_str = re.sub(r'[\u200b-\u200d\ufeff]', '', json_str)
    
    json_str = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]', '', json_str)
    
    return json_str
