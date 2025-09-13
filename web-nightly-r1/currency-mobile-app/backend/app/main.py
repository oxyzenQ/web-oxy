from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import redis.asyncio as redis
from app.core.config import settings
from app.api.routes import currency_router
from app.services.redis_service import RedisService

# Global Redis connection
redis_client = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    global redis_client
    try:
        redis_client = redis.Redis(
            host=settings.REDIS_HOST,
            port=settings.REDIS_PORT,
            decode_responses=True
        )
        await redis_client.ping()
        RedisService.set_client(redis_client)
        print("✅ Connected to Redis")
    except Exception as e:
        print(f"⚠️  Redis connection failed: {e}")
        print("📱 Running without cache")
    
    yield
    
    # Shutdown
    if redis_client:
        await redis_client.close()

app = FastAPI(
    title="Currency Converter API",
    description="Fast and reliable currency conversion API for mobile apps",
    version="1.0.0",
    lifespan=lifespan
)

# CORS middleware for mobile app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(currency_router, prefix="/api/v1", tags=["currency"])

@app.get("/")
async def root():
    return {
        "message": "Currency Converter API",
        "version": "1.0.0",
        "status": "active",
        "endpoints": {
            "currencies": "/api/v1/currencies",
            "convert": "/api/v1/convert",
            "rates": "/api/v1/rates/{base_currency}",
            "health": "/health"
        }
    }

@app.get("/health")
async def health_check():
    redis_status = "connected" if redis_client else "disconnected"
    return {
        "status": "healthy",
        "redis": redis_status,
        "timestamp": "2025-09-06T00:43:23+08:00"
    }
