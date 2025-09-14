from fastapi import FastAPI, HTTPException, Query, Request, Header
from fastapi.middleware.cors import CORSMiddleware
from jose import JWTError, jwt
from dotenv import load_dotenv
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

app = FastAPI(
    title="Kconvert backend API",
    description="Secure proxy API for exchange rates",
    version="1.0.0"
)

# CORS setup
LOCAL_ORIGINS = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://localhost:8080",
    "http://127.0.0.1:8080",
]
OTHER_ORIGINS = os.getenv("OTHER_ORIGINS")
origins = [OTHER_ORIGINS] + LOCAL_ORIGINS if OTHER_ORIGINS else LOCAL_ORIGINS

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

# JWT configuration
SECRET_KEY = os.getenv("JWT_SECRET_KEY")
ALGORITHM = "HS256"
TOKEN_EXP_MINUTES = int(os.getenv("TOKEN_EXP_MINUTES", "10"))
EXCHANGE_API_KEY = os.getenv("EXCHANGE_API_KEY")
BASE_URL = "https://v6.exchangerate-api.com/v6"

# Basic currency list
SUPPORTED_CURRENCIES = [
    {"code": "USD", "name": "US Dollar", "country": "United States"},
    {"code": "EUR", "name": "Euro", "country": "European Union"},
    {"code": "GBP", "name": "British Pound", "country": "United Kingdom"},
    {"code": "JPY", "name": "Japanese Yen", "country": "Japan"},
    {"code": "CAD", "name": "Canadian Dollar", "country": "Canada"},
    {"code": "AUD", "name": "Australian Dollar", "country": "Australia"},
    {"code": "CHF", "name": "Swiss Franc", "country": "Switzerland"},
    {"code": "CNY", "name": "Chinese Yuan", "country": "China"},
    {"code": "INR", "name": "Indian Rupee", "country": "India"},
    {"code": "BRL", "name": "Brazilian Real", "country": "Brazil"}
]

def verify_jwt(token: str):
    """Verify JWT token"""
    if not SECRET_KEY:
        raise HTTPException(status_code=500, detail="JWT not configured")
    
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        if payload.get("exp", 0) < time.time():
            raise HTTPException(status_code=401, detail="Token expired")
        if payload.get("owner") != "oxchin":
            raise HTTPException(status_code=403, detail="Invalid owner")
        return payload
    except JWTError:
        raise HTTPException(status_code=403, detail="Invalid token")

def create_jwt(owner: str = "oxchin", minutes: int = TOKEN_EXP_MINUTES) -> str:
    """Create JWT token"""
    if not SECRET_KEY:
        raise HTTPException(status_code=500, detail="JWT not configured")
    
    now = time.time()
    payload = {
        "owner": owner,
        "iat": now,
        "exp": now + (minutes * 60),
    }
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)

@app.get("/")
async def root():
    return {
        "message": "Currency Converter API",
        "status": "healthy",
        "version": "1.0.0"
    }

@app.get("/api/auth")
async def issue_token(request: Request, origin: Optional[str] = Header(default=None, alias="Origin")):
    """Issue JWT token"""
    if origin and origin not in origins:
        raise HTTPException(status_code=403, detail="Origin not allowed")
    
    token = create_jwt()
    return {
        "token": token,
        "expires_in": TOKEN_EXP_MINUTES * 60
    }

@app.get("/api/rates/{base_currency}")
async def get_exchange_rates(
    request: Request, 
    base_currency: str, 
    token: str = Query(..., description="JWT token"),
    targets: Optional[str] = Query(None, description="Comma-separated target currencies")
):
    """Get exchange rates"""
    # Verify token
    verify_jwt(token)
    
    base_currency = base_currency.upper()
    
    if not EXCHANGE_API_KEY:
        raise HTTPException(status_code=500, detail="Exchange API not configured")
    
    try:
        url = f"{BASE_URL}/{EXCHANGE_API_KEY}/latest/{base_currency}"
        
        async with httpx.AsyncClient() as client:
            response = await client.get(url, timeout=10.0)
            response.raise_for_status()
            data = response.json()
        
        if data.get("result") != "success":
            raise HTTPException(status_code=502, detail="Exchange rate service error")
        
        conversion_rates = data.get("conversion_rates", {})
        if targets:
            target_list = [t.strip().upper() for t in targets.split(',')]
            conversion_rates = {k: v for k, v in conversion_rates.items() if k in target_list}
        
        return {
            "base_currency": base_currency,
            "success": True,
            "conversion_rates": conversion_rates,
            "rates_count": len(conversion_rates),
            "timestamp": time.time()
        }
        
    except httpx.RequestError as e:
        logger.error(f"Request error: {e}")
        raise HTTPException(status_code=502, detail="Exchange rate service unavailable")
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/api/currencies")
async def get_currencies(request: Request):
    """Get supported currencies"""
    return {
        "currencies": SUPPORTED_CURRENCIES,
        "count": len(SUPPORTED_CURRENCIES),
        "timestamp": time.time()
    }

@app.get("/api/regions")
async def get_regions(request: Request):
    """Get regions"""
    regions = list(set(curr.get('region', 'Other') for curr in SUPPORTED_CURRENCIES))
    return {
        "regions": [{"name": region} for region in regions],
        "total_regions": len(regions),
        "timestamp": time.time()
    }

# Vercel serverless function handler
handler = app
