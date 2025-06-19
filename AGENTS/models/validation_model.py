"""Data models for the validation of medical scenario requests.

This module defines the Pydantic models used to structure the output of
validation processes within the scenario generation pipeline.
"""
from __future__ import annotations

from pydantic import BaseModel, Field


class ValidationResult(BaseModel):
    """Represents the outcome of a medical scenario topic validation.

    This model structures the response from a validation check, encapsulating
    both the boolean decision and a human-readable justification. It ensures
    that the validation result can be reliably interpreted by other components.

    Attributes:
        is_valid (bool): True if the request is considered a valid topic for a
            medical scenario, False otherwise.
        reason (str): A concise explanation, in Italian, detailing why the
            request was deemed valid or invalid.
    """
    is_valid: bool = Field(
        ...,
        description="True if the request is a valid medical scenario topic, False otherwise."
    )
    reason: str = Field(
        ...,
        description="A brief explanation in Italian of why the request is or is not valid."
    )