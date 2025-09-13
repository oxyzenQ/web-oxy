from pydantic import BaseModel, Field
from typing import Dict, Optional
from datetime import datetime

class CurrencyCode(BaseModel):
    code: str = Field(..., min_length=3, max_length=3, description="3-letter currency code")
    name: str = Field(..., description="Full currency name")
    country: str = Field(..., description="Country code")

class ConversionRequest(BaseModel):
    from_currency: str = Field(..., min_length=3, max_length=3, description="Source currency code")
    to_currency: str = Field(..., min_length=3, max_length=3, description="Target currency code")
    amount: float = Field(..., gt=0, le=1000000, description="Amount to convert")

class ConversionResponse(BaseModel):
    from_currency: str
    to_currency: str
    amount: float
    converted_amount: float
    exchange_rate: float
    timestamp: datetime
    formatted_result: str

class ExchangeRatesResponse(BaseModel):
    base_currency: str
    rates: Dict[str, float]
    timestamp: datetime
    source: str = "exchangerate-api"

class CurrencyListResponse(BaseModel):
    currencies: Dict[str, str]  # code -> country_code mapping
    count: int
    timestamp: datetime

class HistoricalRateRequest(BaseModel):
    base_currency: str = Field(..., min_length=3, max_length=3)
    target_currency: str = Field(..., min_length=3, max_length=3)
    date: str = Field(..., description="Date in YYYY-MM-DD format")

class APIError(BaseModel):
    error: str
    message: str
    timestamp: datetime
