"""Common utility functions for the Medical Simulation Suite AI.

This module provides helper functions for initializing models and knowledge bases,
reading all configurations from the central config file.
"""

import logging
from typing import Union

from agno.models.anthropic import Claude
from agno.models.google import Gemini

from agno.vectordb.chroma import ChromaDb
from agno.knowledge.json import JSONKnowledgeBase
from agno.embedder.google import GeminiEmbedder
from agno.document.chunking.recursive import RecursiveChunking

from config import MODEL_CONFIG, USE_ANTHROPIC, AI_PROVIDER

embedder = GeminiEmbedder() 
vector_db = ChromaDb(collection="sim_suite_data", path="data/chromadb/general", persistent_client=True, embedder=embedder)

knowledge_base = JSONKnowledgeBase(
    path="./data/case_studies/",
    vector_db=vector_db,
    chunking_strategy=RecursiveChunking()
)

report_vector_db = ChromaDb(collection="sim_suite_reports", path="data/chromadb/reports", persistent_client=True, embedder=embedder)

report_knowledge_base = JSONKnowledgeBase(
    path="./data/reports/",
    vector_db=report_vector_db,
    chunking_strategy=RecursiveChunking()
)

logger = logging.getLogger(__name__)

logger.info(f"AI Provider selected based on API keys: {AI_PROVIDER}")

def get_big_model() -> Union[Gemini, Claude]:
    """
    Initializes and returns the primary generation model based on the
    centralized configuration.
    """
    if USE_ANTHROPIC:
        model_name = MODEL_CONFIG["claude_big"]
        logger.info(f"Initializing BIG model: Claude ('{model_name}')")
        return Claude(model_name)
    else:
        model_name = MODEL_CONFIG["gemini_big"]
        logger.info(f"Initializing BIG model: Gemini ('{model_name}')")
        return Gemini(model_name)


def get_small_model() -> Union[Gemini, Claude]:
    """
    Initializes and returns the smaller/faster model based on the
    centralized configuration.
    """
    if USE_ANTHROPIC:
        model_name = MODEL_CONFIG["claude_small"]
        logger.info(f"Initializing SMALL model: Claude ('{model_name}')")
        return Claude(model_name)
    else:
        model_name = MODEL_CONFIG["gemini_small"]
        logger.info(f"Initializing SMALL model: Gemini ('{model_name}')")
        return Gemini(model_name)

def get_new_model() -> Union[Gemini, Claude]:
    """
    Initializes and returns the latest model based on the
    centralized configuration.
    """
    if USE_ANTHROPIC:
        model_name = MODEL_CONFIG["claude_big"]
        logger.info(f"Initializing NEW model: Claude ('{model_name}')")
        return Claude(model_name)
    else:
        model_name = MODEL_CONFIG["gemini_new"]
        logger.info(f"Initializing NEW model: Gemini ('{model_name}')")
        return Gemini(model_name)
    
def get_knowledge_base() -> JSONKnowledgeBase:
    """Provides access to the knowledge base."""
    return knowledge_base

def get_report_knowledge_base() -> JSONKnowledgeBase:
    """Provides access to the report knowledge base."""
    return report_knowledge_base