from fastapi import APIRouter, HTTPException, Depends
from typing import Dict
from datetime import datetime
from app.models.currency import (
    ConversionRequest, 
    ConversionResponse, 
    ExchangeRatesResponse,
    CurrencyListResponse,
    APIError
)
from app.services.currency_service import CurrencyService

currency_router = APIRouter()

@currency_router.get("/currencies", response_model=CurrencyListResponse)
async def get_currencies():
    """Get all supported currencies with country codes"""
    currencies = CurrencyService.get_supported_currencies()
    return CurrencyListResponse(
        currencies=currencies,
        count=len(currencies),
        timestamp=datetime.now()
    )

@currency_router.post("/convert", response_model=ConversionResponse)
async def convert_currency(request: ConversionRequest):
    """Convert currency amount"""
    # Validate currency codes
    if not CurrencyService.is_valid_currency(request.from_currency):
        raise HTTPException(
            status_code=400, 
            detail=f"Invalid source currency: {request.from_currency}"
        )
    
    if not CurrencyService.is_valid_currency(request.to_currency):
        raise HTTPException(
            status_code=400, 
            detail=f"Invalid target currency: {request.to_currency}"
        )
    
    # Perform conversion
    result = await CurrencyService.convert_currency(
        request.from_currency.upper(),
        request.to_currency.upper(),
        request.amount
    )
    
    if not result:
        raise HTTPException(
            status_code=503,
            detail="Currency conversion service temporarily unavailable"
        )
    
    return result

@currency_router.get("/rates/{base_currency}", response_model=ExchangeRatesResponse)
async def get_exchange_rates(base_currency: str):
    """Get all exchange rates for a base currency"""
    base_currency = base_currency.upper()
    
    if not CurrencyService.is_valid_currency(base_currency):
        raise HTTPException(
            status_code=400,
            detail=f"Invalid base currency: {base_currency}"
        )
    
    rates = await CurrencyService.get_exchange_rates(base_currency)
    
    if not rates:
        raise HTTPException(
            status_code=503,
            detail="Exchange rate service temporarily unavailable"
        )
    
    return ExchangeRatesResponse(
        base_currency=base_currency,
        rates=rates,
        timestamp=datetime.now(),
        source="exchangerate-api"
    )

@currency_router.get("/rate/{from_currency}/{to_currency}")
async def get_single_rate(from_currency: str, to_currency: str):
    """Get exchange rate between two currencies"""
    from_currency = from_currency.upper()
    to_currency = to_currency.upper()
    
    if not CurrencyService.is_valid_currency(from_currency):
        raise HTTPException(status_code=400, detail=f"Invalid currency: {from_currency}")
    
    if not CurrencyService.is_valid_currency(to_currency):
        raise HTTPException(status_code=400, detail=f"Invalid currency: {to_currency}")
    
    rates = await CurrencyService.get_exchange_rates(from_currency)
    
    if not rates or to_currency not in rates:
        raise HTTPException(
            status_code=503,
            detail="Exchange rate not available"
        )
    
    return {
        "from_currency": from_currency,
        "to_currency": to_currency,
        "exchange_rate": rates[to_currency],
        "timestamp": datetime.now()
    }
