from fastapi import FastAPI, Depends, HTTPException, Query, Request, Header
from fastapi.middleware.cors import CORSMiddleware
from jose import JWTError, jwt
from dotenv import load_dotenv
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
import os
from typing import Optional
import httpx
import time
import logging

# Load environment variables
load_dotenv()

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Rate limiter setup (configurable)
RATE_LIMIT_PER_MINUTE = int(os.getenv("RATE_LIMIT_PER_MINUTE", "30"))
AUTH_RATE_LIMIT_PER_MINUTE = int(os.getenv("AUTH_RATE_LIMIT_PER_MINUTE", "20"))
limiter = Limiter(key_func=get_remote_address)
app = FastAPI(
    title="Currency Converter API",
    description="Secure proxy API for exchange rates with JWT authentication",
    version="1.0.0"
)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# CORS setup - restrict to configured frontend domain(s)
LOCAL_ORIGINS = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://localhost:8080",
    "http://127.0.0.1:8080",
]
SERVERFRONTEND = os.getenv("SERVERFRONTEND")
origins = [SERVERFRONTEND] + LOCAL_ORIGINS

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

# JWT and API configuration
SECRET_KEY = os.getenv("JWT_SECRET")
ALGORITHM = "HS256"
TOKEN_EXP_MINUTES = int(os.getenv("TOKEN_EXP_MINUTES", "10"))
EXCHANGE_API_KEY = os.getenv("EXCHANGE_API_KEY")
BASE_URL = "https://v6.exchangerate-api.com/v6"

if not EXCHANGE_API_KEY:
    logger.error("EXCHANGE_API_KEY not found in environment variables!")
    raise ValueError("EXCHANGE_API_KEY is required")

def verify_jwt(token: str):
    """Verify JWT token and return payload if valid"""
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        
        # Check expiration
        if payload.get("exp", 0) < time.time():
            raise HTTPException(status_code=401, detail="Token expired")
        
        # Check if owner
        if payload.get("owner") != "oxchin":
            raise HTTPException(status_code=403, detail="Invalid owner")
            
        return payload
    except JWTError as e:
        logger.warning(f"JWT verification failed: {e}")
        raise HTTPException(status_code=403, detail="Invalid token")

def create_jwt(owner: str = "oxchin", minutes: int = TOKEN_EXP_MINUTES, purpose: str = "currency_api_access") -> str:
    """Create a short-lived JWT token for frontend usage"""
    now = time.time()
    payload = {
        "owner": owner,
        "iat": now,
        "exp": now + (minutes * 60),
        "purpose": purpose,
    }
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

@app.get("/")
async def root():
    """Health check endpoint"""
    return {
        "message": "Currency Converter API is running",
        "status": "healthy",
        "timestamp": time.time()
    }

@app.get("/api/auth")
@limiter.limit(f"{AUTH_RATE_LIMIT_PER_MINUTE}/minute")
async def issue_token(request: Request, origin: Optional[str] = Header(default=None, alias="Origin")):
    """Issue a short-lived JWT for frontend usage.
    In production, you may restrict by origin or add additional checks.
    """
    # Strict origin check in production-like scenarios
    if origin and origin not in origins:
        raise HTTPException(status_code=403, detail="Origin not allowed")
    token = create_jwt()
    return {
        "token": token,
        "expires_in": TOKEN_EXP_MINUTES * 60
    }

@app.get("/api/rates/{base_currency}")
@limiter.limit(f"{RATE_LIMIT_PER_MINUTE}/minute")
async def get_exchange_rates(
    request: Request,
    base_currency: str,
    token: Optional[str] = Query(default=None, description="JWT authentication token (deprecated; use Authorization header)"),
    authorization: Optional[str] = Header(default=None, alias="Authorization")
):
    """
    Get exchange rates for a base currency
    Requires valid JWT token for authentication
    """
    # Extract token from Authorization header first (Bearer), fallback to query param
    jwt_token: Optional[str] = None
    if authorization:
        try:
            scheme, value = authorization.split(" ", 1)
            if scheme.lower() == "bearer" and value:
                jwt_token = value.strip()
        except ValueError:
            raise HTTPException(status_code=401, detail="Invalid Authorization header format")

    if not jwt_token:
        jwt_token = token

    if not jwt_token:
        raise HTTPException(status_code=401, detail="Missing token")

    # Verify JWT token
    verify_jwt(jwt_token)
    
    # Validate currency code (3 letters, uppercase)
    base_currency = base_currency.upper()
    if len(base_currency) != 3 or not base_currency.isalpha():
        raise HTTPException(status_code=400, detail="Invalid currency code")
    
    # Check if currency is supported
    if base_currency not in SUPPORTED_CURRENCIES:
        raise HTTPException(status_code=400, detail=f"Currency {base_currency} not supported. Use /api/currencies for supported list.")
    
    try:
        # Fetch data from Exchange Rate API
        url = f"{BASE_URL}/{EXCHANGE_API_KEY}/latest/{base_currency}"
        
        async with httpx.AsyncClient(timeout=10.0) as client:
            response = await client.get(url)
            
            if response.status_code != 200:
                logger.error(f"Exchange API error: {response.status_code}")
                raise HTTPException(
                    status_code=502, 
                    detail="Exchange rate service unavailable"
                )
            
            data = response.json()
            
            # Check if API returned error
            if data.get("result") == "error":
                error_type = data.get("error-type", "unknown")
                logger.error(f"Exchange API error: {error_type}")
                raise HTTPException(status_code=400, detail=f"API error: {error_type}")
            
            # Log successful request
            logger.info(f"Exchange rates fetched for {base_currency}")
            
            return {
                "success": True,
                "base_code": data.get("base_code"),
                "conversion_rates": data.get("conversion_rates"),
                "time_last_update": data.get("time_last_update_utc"),
                "time_next_update": data.get("time_next_update_utc")
            }
            
    except httpx.TimeoutException:
        logger.error("Exchange API timeout")
        raise HTTPException(status_code=504, detail="Exchange rate service timeout")
    except httpx.RequestError as e:
        logger.error(f"Exchange API request error: {e}")
        raise HTTPException(status_code=502, detail="Exchange rate service error")
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")

# Supported currencies - sync with frontend main.js
SUPPORTED_CURRENCIES = {
    'USD': 'US Dollar',
    'EUR': 'Euro', 
    'GBP': 'British Pound',
    'JPY': 'Japanese Yen',
    'AUD': 'Australian Dollar',
    'CAD': 'Canadian Dollar',
    'CHF': 'Swiss Franc',
    'CNY': 'Chinese Yuan',
    'SEK': 'Swedish Krona',
    'NZD': 'New Zealand Dollar',
    'MXN': 'Mexican Peso',
    'SGD': 'Singapore Dollar',
    'HKD': 'Hong Kong Dollar',
    'NOK': 'Norwegian Krone',
    'KRW': 'South Korean Won',
    'TRY': 'Turkish Lira',
    'RUB': 'Russian Ruble',
    'INR': 'Indian Rupee',
    'BRL': 'Brazilian Real',
    'ZAR': 'South African Rand',
    'DKK': 'Danish Krone',
    'PLN': 'Polish Zloty',
    'TWD': 'Taiwan Dollar',
    'THB': 'Thai Baht',
    'IDR': 'Indonesian Rupiah',
    'HUF': 'Hungarian Forint',
    'CZK': 'Czech Koruna',
    'ILS': 'Israeli Shekel',
    'CLP': 'Chilean Peso',
    'PHP': 'Philippine Peso',
    'AED': 'UAE Dirham',
    'COP': 'Colombian Peso',
    'SAR': 'Saudi Riyal',
    'MYR': 'Malaysian Ringgit',
    'RON': 'Romanian Leu'
}

@app.get("/api/currencies")
async def get_supported_currencies():
    """Get list of supported currencies"""
    return {
        "currencies": [
            {"code": code, "name": name} 
            for code, name in SUPPORTED_CURRENCIES.items()
        ]
    }

@app.get("/api/health")
async def health_check():
    """Detailed health check for monitoring"""
    return {
        "status": "healthy",
        "timestamp": time.time(),
        "api_key_configured": bool(EXCHANGE_API_KEY),
        "jwt_secret_configured": bool(SECRET_KEY and SECRET_KEY),
        "token_exp_minutes": TOKEN_EXP_MINUTES,
        "rate_limit_per_minute": RATE_LIMIT_PER_MINUTE,
        "auth_rate_limit_per_minute": AUTH_RATE_LIMIT_PER_MINUTE,
        "allowed_origins": origins,
        "supported_currencies_count": len(SUPPORTED_CURRENCIES),
    }

# Security headers middleware
@app.middleware("http")
async def add_security_headers(request: Request, call_next):
    response = await call_next(request)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["Referrer-Policy"] = "no-referrer"
    response.headers["Permissions-Policy"] = "geolocation=(), microphone=(), camera=()"
    # Only set HSTS when behind HTTPS in production
    if request.url.scheme == "https":
        response.headers["Strict-Transport-Security"] = "max-age=63072000; includeSubDomains; preload"
    # Prevent caching of API responses
    response.headers["Cache-Control"] = "no-store"
    return response

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)