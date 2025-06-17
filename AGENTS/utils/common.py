"""Common utility functions for the Medical Simulation Suite AI.

This module provides a collection of helper functions designed to support various
tasks within the application. These utilities include model initialization for
different generation tasks and robust JSON parsing from model responses.

Version: 4.3
"""

import json
import logging
import os
import re
from typing import Any, Dict, Optional, Union

from agno.models.anthropic import Claude
from agno.models.google import Gemini
from dotenv import load_dotenv

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

load_dotenv()

use_anthorpic = False
if not os.getenv("GOOGLE_API_KEY"):
    logger.warning("GOOGLE_API_KEY not found. Attempting to fall back to Anthropic.")
    if os.getenv("ANTHROPIC_API_KEY"):
        use_anthorpic = True
        logger.info("Using Anthropic (Claude) as the AI provider.")
    else:
        raise EnvironmentError(
            "No API key found. Please set either GOOGLE_API_KEY or ANTHROPIC_API_KEY in your environment."
        )
else:
    logger.info("Using Google (Gemini) as the AI provider.")


def get_model() -> Union[Gemini, Claude]:
    """Initializes and returns the primary generation model.

    Returns an instance of Gemini ('gemini-2.0-flash') if GOOGLE_API_KEY is set.
    Otherwise, falls back to Anthropic ('claude-3.5-sonnet') if ANTHROPIC_API_KEY is set.

    Returns:
        An instance of the configured AI model (Gemini or Anthropic).
    """
    if use_anthorpic:
        return Claude("claude-3-5-haiku-20241022") # claude-3-7-sonnet-20250219 claude-3-5-haiku-20241022
    else:
        return Gemini("gemini-2.0-flash") # gemini-2.0-flash gemini-1.5-flash-latest gemini-2.5-flash


def get_exam_model() -> Union[Gemini, Claude]:
    """Initializes and returns the model for exam generation.

    Returns an instance of Gemini ('gemini-1.5-flash-latest') if GOOGLE_API_KEY is set.
    Otherwise, falls back to Anthropic ('claude-3.5-sonnet') if ANTHROPIC_API_KEY is set.

    Returns:
        An instance of the configured AI model (Gemini or Anthropic).
    """
    if use_anthorpic:
        return Claude("claude-3-5-haiku-20241022") # claude-3-7-sonnet-20250219 claude-3-5-haiku-20241022
    else:
        return Gemini("gemini-1.5-flash-latest") # gemini-2.0-flash gemini-1.5-flash-latest gemini-2.5-flash


def extract_json_from_response(response_text: Optional[str]) -> Dict[str, Any]:
    """Extracts a JSON object from a model's text response.

    This function searches for a JSON object within a string, which may be
    plain or enclosed in a markdown code block (```json ... ```). It attempts
    to parse the found JSON string into a Python dictionary.

    Args:
        response_text: The text string returned from the AI model.

    Returns:
        A dictionary containing the parsed JSON data.

    Raises:
        ValueError: If the response text is empty, no JSON is found, or the
            extracted string is not valid JSON.
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
        # Sanifica la stringa prima del parsing per una maggiore robustezza
        sanitized_str = sanitize_json_string(json_str)
        return json.loads(sanitized_str.strip())
    except json.JSONDecodeError as e:
        logger.error(f"JSON Decode Error: {e}\nResponse text received: {json_str}")
        raise ValueError(f"Invalid JSON in AI response: {e}") from e
    except Exception as e:
        logger.error(f"Unexpected error parsing JSON: {e}\nResponse text received: {json_str}")
        raise ValueError("An unexpected error occurred while parsing JSON.") from e


def sanitize_json_string(json_str: str) -> str:
    """Sanitizes a string to improve its compatibility with JSON parsers.

    This function removes or replaces characters that are known to cause issues
    during JSON decoding, such as zero-width spaces and control characters.

    Args:
        json_str: The JSON string to be sanitized.

    Returns:
        A cleaned JSON string.
    """
    # Replace non-standard quotes
    json_str = json_str.replace('“', '"').replace('”', '"')
    json_str = json_str.replace("‘", "'").replace("’", "'")

    # Remove zero-width spaces and other invisible characters
    json_str = re.sub(r'[\u200b-\u200d\ufeff]', '', json_str)

    # Remove control characters except for tab, newline, and carriage return
    json_str = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]', '', json_str)

    return json_str