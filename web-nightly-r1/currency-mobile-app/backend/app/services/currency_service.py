import httpx
import json
from typing import Dict, Optional, Tuple
from datetime import datetime
from app.core.config import settings
from app.services.redis_service import RedisService
from app.models.currency import ConversionResponse, ExchangeRatesResponse

class CurrencyService:
    
    # Currency mappings from original project
    CURRENCY_COUNTRIES = {
        "AED": "AE", "AFN": "AF", "XCD": "AG", "ALL": "AL", "AMD": "AM", "ANG": "AN",
        "AOA": "AO", "AQD": "AQ", "ARS": "AR", "AUD": "AU", "AZN": "AZ", "BAM": "BA",
        "BBD": "BB", "BDT": "BD", "XOF": "BE", "BGN": "BG", "BHD": "BH", "BIF": "BI",
        "BMD": "BM", "BND": "BN", "BOB": "BO", "BRL": "BR", "BSD": "BS", "NOK": "BV",
        "BWP": "BW", "BYR": "BY", "BZD": "BZ", "CAD": "CA", "CDF": "CD", "XAF": "CF",
        "CHF": "CH", "CLP": "CL", "CNY": "CN", "COP": "CO", "CRC": "CR", "CUP": "CU",
        "CVE": "CV", "CYP": "CY", "CZK": "CZ", "DJF": "DJ", "DKK": "DK", "DOP": "DO",
        "DZD": "DZ", "ECS": "EC", "EEK": "EE", "EGP": "EG", "ETB": "ET", "EUR": "FR",
        "FJD": "FJ", "FKP": "FK", "GBP": "GB", "GEL": "GE", "GGP": "GG", "GHS": "GH",
        "GIP": "GI", "GMD": "GM", "GNF": "GN", "GTQ": "GT", "GYD": "GY", "HKD": "HK",
        "HNL": "HN", "HRK": "HR", "HTG": "HT", "HUF": "HU", "IDR": "ID", "ILS": "IL",
        "INR": "IN", "IQD": "IQ", "IRR": "IR", "ISK": "IS", "JMD": "JM", "JOD": "JO",
        "JPY": "JP", "KES": "KE", "KGS": "KG", "KHR": "KH", "KMF": "KM", "KPW": "KP",
        "KRW": "KR", "KWD": "KW", "KYD": "KY", "KZT": "KZ", "LAK": "LA", "LBP": "LB",
        "LKR": "LK", "LRD": "LR", "LSL": "LS", "LTL": "LT", "LVL": "LV", "LYD": "LY",
        "MAD": "MA", "MDL": "MD", "MGA": "MG", "MKD": "MK", "MMK": "MM", "MNT": "MN",
        "MOP": "MO", "MRO": "MR", "MTL": "MT", "MUR": "MU", "MVR": "MV", "MWK": "MW",
        "MXN": "MX", "MYR": "MY", "MZN": "MZ", "NAD": "NA", "XPF": "NC", "NGN": "NG",
        "NIO": "NI", "NPR": "NP", "NZD": "NZ", "OMR": "OM", "PAB": "PA", "PEN": "PE",
        "PGK": "PG", "PHP": "PH", "PKR": "PK", "PLN": "PL", "PYG": "PY", "QAR": "QA",
        "RON": "RO", "RSD": "RS", "RUB": "RU", "RWF": "RW", "SAR": "SA", "SBD": "SB",
        "SCR": "SC", "SDG": "SD", "SEK": "SE", "SGD": "SG", "SKK": "SK", "SLL": "SL",
        "SOS": "SO", "SRD": "SR", "STD": "ST", "SVC": "SV", "SYP": "SY", "SZL": "SZ",
        "THB": "TH", "TJS": "TJ", "TMT": "TM", "TND": "TN", "TOP": "TO", "TRY": "TR",
        "TTD": "TT", "TWD": "TW", "TZS": "TZ", "UAH": "UA", "UGX": "UG", "USD": "US",
        "UYU": "UY", "UZS": "UZ", "VEF": "VE", "VND": "VN", "VUV": "VU", "YER": "YE",
        "ZAR": "ZA", "ZMK": "ZM", "ZWD": "ZW"
    }
    
    @classmethod
    async def get_exchange_rates(cls, base_currency: str) -> Optional[Dict[str, float]]:
        """Get exchange rates for a base currency with caching"""
        cache_key = f"rates:{base_currency}"
        
        # Try cache first
        cached_rates = await RedisService.get_json(cache_key)
        if cached_rates:
            return cached_rates
        
        # Fetch from API
        rates = await cls._fetch_rates_from_api(base_currency)
        if rates:
            # Cache for 1 hour
            await RedisService.set(cache_key, rates, ttl=3600)
        
        return rates
    
    @classmethod
    async def _fetch_rates_from_api(cls, base_currency: str) -> Optional[Dict[str, float]]:
        """Fetch rates from external API"""
        url = f"{settings.EXCHANGE_API_URL}/{settings.EXCHANGE_API_KEY}/latest/{base_currency}"
        
        async with httpx.AsyncClient() as client:
            try:
                response = await client.get(url, timeout=10.0)
                response.raise_for_status()
                data = response.json()
                
                if data.get("result") == "success":
                    return data.get("conversion_rates", {})
                else:
                    print(f"API Error: {data.get('error-type', 'Unknown error')}")
                    return None
                    
            except httpx.RequestError as e:
                print(f"Request error: {e}")
                return None
            except httpx.HTTPStatusError as e:
                print(f"HTTP error: {e}")
                return None
    
    @classmethod
    async def convert_currency(cls, from_currency: str, to_currency: str, amount: float) -> Optional[ConversionResponse]:
        """Convert currency with caching and formatting"""
        rates = await cls.get_exchange_rates(from_currency)
        
        if not rates or to_currency not in rates:
            return None
        
        exchange_rate = rates[to_currency]
        converted_amount = round(amount * exchange_rate, 2)
        
        # Format result similar to original project
        formatted_result = f"{amount:.2f} {from_currency} = {converted_amount:.2f} {to_currency}"
        
        return ConversionResponse(
            from_currency=from_currency,
            to_currency=to_currency,
            amount=amount,
            converted_amount=converted_amount,
            exchange_rate=exchange_rate,
            timestamp=datetime.now(),
            formatted_result=formatted_result
        )
    
    @classmethod
    def get_supported_currencies(cls) -> Dict[str, str]:
        """Get all supported currencies with country codes"""
        return cls.CURRENCY_COUNTRIES
    
    @classmethod
    def is_valid_currency(cls, currency_code: str) -> bool:
        """Check if currency code is supported"""
        return currency_code.upper() in cls.CURRENCY_COUNTRIES
