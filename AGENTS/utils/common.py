"""Common utility functions for the Medical Simulation Suite AI.

This module provides a collection of helper functions designed to support various
tasks within the application. These utilities include model initialization for
different generation tasks and robust JSON parsing from model responses.

Version: 4.3
"""

import logging
import os
from typing import Union

from agno.models.anthropic import Claude
from agno.models.google import Gemini
from dotenv import load_dotenv

from agno.vectordb.chroma import ChromaDb
from agno.knowledge.json import JSONKnowledgeBase
from agno.embedder.google import GeminiEmbedder
from agno.document.chunking.recursive import RecursiveChunking

vector_db = ChromaDb(collection="sim_suite_data", path="data/chromadb/general", persistent_client=True, embedder=GeminiEmbedder())

knowledge_base = JSONKnowledgeBase(
    path="./data/case_studies/",
    vector_db=vector_db,
    chunking_strategy=RecursiveChunking()
)

report_vector_db = ChromaDb(collection="sim_suite_reports", path="data/chromadb/reports", persistent_client=True, embedder=GeminiEmbedder())

report_knowledge_base = JSONKnowledgeBase(
    path="./data/reports/",
    vector_db=report_vector_db,
    chunking_strategy=RecursiveChunking()
)


# Logger instance
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


def get_big_model() -> Union[Gemini, Claude]:
    """Initializes and returns the primary generation model.

    Returns an instance of Gemini ('gemini-2.0-flash') if GOOGLE_API_KEY is set.
    Otherwise, falls back to Anthropic ('claude-3-7-sonnet-20250219') if ANTHROPIC_API_KEY is set.

    Returns:
        An instance of the configured AI model (Gemini or Anthropic).
    """
    if use_anthorpic:
        return Claude("claude-3-7-sonnet-20250219") # claude-3-7-sonnet-20250219 claude-3-5-haiku-20241022
    else:
        return Gemini("gemini-2.0-flash") # gemini-2.0-flash gemini-1.5-flash-latest gemini-2.5-flash


def get_small_model() -> Union[Gemini, Claude]:
    """Initializes and returns the model for exam generation.

    Returns an instance of Gemini ('gemini-1.5-flash-latest') if GOOGLE_API_KEY is set.
    Otherwise, falls back to Anthropic ('claude-3-5-haiku-20241022') if ANTHROPIC_API_KEY is set.

    Returns:
        An instance of the configured AI model (Gemini or Anthropic).
    """
    if use_anthorpic:
        return Claude("claude-3-5-haiku-20241022") # claude-3-7-sonnet-20250219 claude-3-5-haiku-20241022
    else:
        return Gemini("gemini-1.5-flash-latest") # gemini-2.0-flash gemini-1.5-flash-latest gemini-2.5-flash

def get_knowledge_base() -> JSONKnowledgeBase:
    """Provides access to the knowledge base.

    Returns:
        The initialized JSONKnowledgeBase instance.
    """
    return knowledge_base

def get_report_knowledge_base() -> JSONKnowledgeBase:
    """Provides access to the report knowledge base.

    Returns:
        The initialized JSONKnowledgeBase instance for reports.
    """
    return report_knowledge_base