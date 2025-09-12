from fastapi import FastAPI, Depends, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from jose import JWTError, jwt
from dotenv import load_dotenv
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
import os
import httpx
import time
import logging

# Load environment variables
load_dotenv()

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Rate limiter setup
limiter = Limiter(key_func=get_remote_address)
app = FastAPI(
    title="Currency Converter API",
    description="Secure proxy API for exchange rates with JWT authentication",
    version="1.0.0"
)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# CORS setup - restrict to your Vercel domain
origins = [
    "https://oxyme.vercel.app",  # Replace with actual Vercel domain
    "http://localhost:3000",  # For local development
    "http://127.0.0.1:3000",
    "http://localhost:8080",
    "http://127.0.0.1:8080"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

# JWT and API configuration
SECRET_KEY = os.getenv("JWT_SECRET", "your-super-secret-jwt-key-change-this")
ALGORITHM = "HS256"
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
        if payload.get("owner") != "kirai":
            raise HTTPException(status_code=403, detail="Invalid owner")
            
        return payload
    except JWTError as e:
        logger.warning(f"JWT verification failed: {e}")
        raise HTTPException(status_code=403, detail="Invalid token")

@app.get("/")
async def root():
    """Health check endpoint"""
    return {
        "message": "Currency Converter API is running",
        "status": "healthy",
        "timestamp": time.time()
    }

@app.get("/api/rates/{base_currency}")
@limiter.limit("30/minute")  # Rate limit: 30 requests per minute per IP
async def get_exchange_rates(
    request,
    base_currency: str,
    token: str = Query(..., description="JWT authentication token")
):
    """
    Get exchange rates for a base currency
    Requires valid JWT token for authentication
    """
    # Verify JWT token
    verify_jwt(token)
    
    # Validate currency code (3 letters, uppercase)
    base_currency = base_currency.upper()
    if len(base_currency) != 3 or not base_currency.isalpha():
        raise HTTPException(status_code=400, detail="Invalid currency code")
    
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

@app.get("/api/health")
async def health_check():
    """Detailed health check for monitoring"""
    return {
        "status": "healthy",
        "timestamp": time.time(),
        "api_key_configured": bool(EXCHANGE_API_KEY),
        "jwt_secret_configured": bool(SECRET_KEY and SECRET_KEY != "your-super-secret-jwt-key-change-this")
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
