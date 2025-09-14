from fastapi import FastAPI, Depends, HTTPException, Query, Request, Header
from fastapi.middleware.cors import CORSMiddleware
from jose import JWTError, jwt
from dotenv import load_dotenv
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
import os
from typing import Optional, List, Dict, Any
import httpx
import time
import logging
import asyncio
from contextlib import asynccontextmanager

# Import enhanced modules
from currency_data import (
    SUPPORTED_CURRENCIES, 
    CURRENCY_SEARCH_MAPPINGS,
    get_currencies_by_priority,
    get_currencies_by_region,
    get_top_currencies
)
from cache_manager import (
    cached_exchange_rates,
    cached_currency_list,
    exchange_rate_cache,
    currency_list_cache,
    CacheMonitor,
    warm_cache_with_popular_currencies
)
from performance_optimizer import (
    HTTPConnectionManager,
    PerformanceMonitor,
    get_http_client
)

# Load environment variables
load_dotenv()

# Production-optimized logging setup
PRODUCTION_MODE = os.getenv('PRODUCTION_MODE', 'false').lower() == 'true'

if PRODUCTION_MODE:
    # Minimal logging for production
    logging.basicConfig(
        level=logging.ERROR,
        format='%(levelname)s: %(message)s',
        handlers=[logging.StreamHandler()]
    )
else:
    # Enhanced logging for development
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler(),
            logging.FileHandler('currency_api.log')
        ]
    )

logger = logging.getLogger(__name__)

# Disable httpx logging in production
if PRODUCTION_MODE:
    logging.getLogger('httpx').setLevel(logging.ERROR)
    logging.getLogger('httpcore').setLevel(logging.ERROR)
    logging.getLogger('cache_manager').setLevel(logging.ERROR)

# Global performance monitor
performance_monitor = PerformanceMonitor()

# Rate limiter setup (configurable)
RATE_LIMIT_PER_MINUTE = int(os.getenv("RATE_LIMIT_PER_MINUTE", "30"))
AUTH_RATE_LIMIT_PER_MINUTE = int(os.getenv("AUTH_RATE_LIMIT_PER_MINUTE", "20"))
limiter = Limiter(key_func=get_remote_address)

# Lifespan context manager for startup/shutdown
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    if not PRODUCTION_MODE:
        logger.info("🚀 Starting Currency Converter API with enhanced features")
        logger.info(f"📊 Supporting {len(SUPPORTED_CURRENCIES)} currencies")
    
    # Warm up cache with popular currencies
    await warm_cache_with_popular_currencies()
    
    yield
    
    # Shutdown
    if not PRODUCTION_MODE:
        logger.info("🛑 Shutting down Currency Converter API")
        CacheMonitor.log_performance_metrics()

app = FastAPI(
    title="Currency Converter API - Enhanced",
    description="Production-grade secure proxy API for exchange rates with advanced caching, 100+ currencies, and intelligent filtering",
    version="2.0.0",
    lifespan=lifespan
)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# Enhanced CORS setup with multiple environments
LOCAL_ORIGINS = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://localhost:3001",
    "http://127.0.0.1:3001",
    "http://localhost:3002",
    "http://127.0.0.1:3002",
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

# JWT and API configuration
SECRET_KEY = os.getenv("JWT_SECRET_KEY")
ALGORITHM = "HS256"
TOKEN_EXP_MINUTES = int(os.getenv("TOKEN_EXP_MINUTES", "10"))
EXCHANGE_API_KEY = os.getenv("EXCHANGE_API_KEY")
BASE_URL = "https://v6.exchangerate-api.com/v6"

# Enhanced validation
if not SECRET_KEY:
    logger.error("JWT_SECRET_KEY not found in environment variables!")
    raise ValueError("JWT_SECRET_KEY is required for secure operation")

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
    """Enhanced health check endpoint with system status"""
    cache_stats = CacheMonitor.get_comprehensive_stats()
    perf_stats = performance_monitor.get_performance_summary()
    
    return {
        "message": "Currency Converter API - Enhanced Edition",
        "status": "healthy",
        "version": "2.0.0",
        "features": {
            "currencies_supported": len(SUPPORTED_CURRENCIES),
            "regions_covered": len(set(curr['region'] for curr in SUPPORTED_CURRENCIES)),
            "caching_enabled": True,
            "performance_monitoring": True,
            "intelligent_search": True
        },
        "cache_performance": {
            "exchange_rates_hit_rate": cache_stats.get('exchange_rates', {}).get('hit_rate', 0),
            "currency_lists_hit_rate": cache_stats.get('currency_lists', {}).get('hit_rate', 0)
        },
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
    token: str = Depends(verify_jwt),
    targets: Optional[str] = Query(None, description="Comma-separated target currencies")
):
    """Get exchange rates with enhanced caching and performance optimization"""
    base_currency = base_currency.upper()
    start_time = time.time()
    
    if not EXCHANGE_API_KEY:
        logger.error("EXCHANGE_API_KEY not configured")
        raise HTTPException(status_code=500, detail="Exchange API not configured")
    
    # Validate base currency
    valid_currencies = [curr['code'] for curr in SUPPORTED_CURRENCIES]
    if base_currency not in valid_currencies:
        raise HTTPException(status_code=400, detail=f"Unsupported base currency: {base_currency}")
    
    try:
        # Use cached exchange rates with optimized HTTP client
        async def fetch_rates():
            url = f"{BASE_URL}/{EXCHANGE_API_KEY}/latest/{base_currency}"
            
            async with get_http_client() as http_client:
                response_data = await http_client.get_with_retry(url)
                
                if not response_data:
                    raise HTTPException(status_code=502, detail="Exchange rate service unavailable")
                
                return response_data
        
        data = await cached_exchange_rates(base_currency, fetch_rates)
        
        # Filter target currencies if specified
        conversion_rates = data.get("conversion_rates", {})
        if targets:
            target_list = [t.strip().upper() for t in targets.split(',')]
            conversion_rates = {k: v for k, v in conversion_rates.items() if k in target_list}
        
        response_time = time.time() - start_time
        performance_monitor.record_request(f'/api/rates/{base_currency}', response_time, 200)
        
        return {
            "base_currency": base_currency,
            "success": data.get("result") == "success",
            "conversion_rates": conversion_rates,
            "rates_count": len(conversion_rates),
            "cache_hit": True,  # This would be set by caching layer
            "response_time_ms": round(response_time * 1000, 2),
            "timestamp": time.time(),
            "last_updated": data.get("time_last_update_utc")
        }
                
    except HTTPException:
        raise
    except Exception as e:
        response_time = time.time() - start_time
        performance_monitor.record_request(f'/api/rates/{base_currency}', response_time, 500)
        logger.error(f"Unexpected error fetching rates for {base_currency}: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.post("/api/rates/batch")
@limiter.limit(f"{RATE_LIMIT_PER_MINUTE}/minute")
async def get_batch_exchange_rates(
    request: Request,
    currencies: List[str],
    token: str = Depends(verify_jwt)
):
    """Get exchange rates for multiple currencies in parallel"""
    start_time = time.time()
    
    if not EXCHANGE_API_KEY:
        logger.error("EXCHANGE_API_KEY not configured")
        raise HTTPException(status_code=500, detail="Exchange API not configured")
    
    # Validate and clean currency codes
    valid_currencies = [curr['code'] for curr in SUPPORTED_CURRENCIES]
    currencies = [curr.upper() for curr in currencies if curr.upper() in valid_currencies]
    
    if not currencies:
        raise HTTPException(status_code=400, detail="No valid currencies provided")
    
    if len(currencies) > 10:  # Limit batch size
        raise HTTPException(status_code=400, detail="Maximum 10 currencies per batch request")
    
    try:
        # Parallel fetch function
        async def fetch_single_rate(currency: str):
            async def fetch_rates():
                url = f"{BASE_URL}/{EXCHANGE_API_KEY}/latest/{currency}"
                async with get_http_client() as http_client:
                    return await http_client.get_with_retry(url)
            
            try:
                data = await cached_exchange_rates(currency, fetch_rates)
                return {
                    "currency": currency,
                    "success": True,
                    "data": data
                }
            except Exception as e:
                logger.error(f"Error fetching rates for {currency}: {e}")
                return {
                    "currency": currency,
                    "success": False,
                    "error": str(e)
                }
        
        # Execute all requests in parallel
        results = await asyncio.gather(*[fetch_single_rate(curr) for curr in currencies])
        
        # Process results
        successful_results = {}
        failed_results = {}
        
        for result in results:
            if result["success"]:
                successful_results[result["currency"]] = {
                    "conversion_rates": result["data"].get("conversion_rates", {}),
                    "last_updated": result["data"].get("time_last_update_utc")
                }
            else:
                failed_results[result["currency"]] = result["error"]
        
        response_time = time.time() - start_time
        performance_monitor.record_request('/api/rates/batch', response_time, 200)
        
        return {
            "successful_currencies": list(successful_results.keys()),
            "failed_currencies": list(failed_results.keys()),
            "results": successful_results,
            "errors": failed_results,
            "total_requested": len(currencies),
            "successful_count": len(successful_results),
            "response_time_ms": round(response_time * 1000, 2),
            "timestamp": time.time()
        }
        
    except Exception as e:
        response_time = time.time() - start_time
        performance_monitor.record_request('/api/rates/batch', response_time, 500)
        logger.error(f"Batch exchange rates error: {e}")
        raise HTTPException(status_code=500, detail="Batch request failed")

@app.get("/api/currencies")
@limiter.limit(f"{RATE_LIMIT_PER_MINUTE}/minute")
async def get_currencies(
    request: Request,
    priority: Optional[int] = Query(None, description="Filter by priority level (1=highest, 3=lowest)"),
    region: Optional[str] = Query(None, description="Filter by region"),
    limit: Optional[int] = Query(None, description="Limit number of results"),
    search: Optional[str] = Query(None, description="Search currencies by name, code, or country")
):
    """Get currencies with advanced filtering and search capabilities"""
    try:
        start_time = time.time()
        
        # Use caching for currency list
        filters = {
            'priority': priority,
            'region': region,
            'limit': limit,
            'search': search
        }
        
        async def fetch_currencies():
            currencies = SUPPORTED_CURRENCIES.copy()
            
            # Apply filters
            if priority:
                currencies = [curr for curr in currencies if curr['priority'] == priority]
            
            if region:
                currencies = [curr for curr in currencies if curr['region'].lower() == region.lower()]
            
            if search:
                search_term = search.lower().strip()
                filtered_currencies = []
                
                for curr in currencies:
                    # Search in code, name, country, and smart mappings
                    if (search_term in curr['code'].lower() or
                        search_term in curr['name'].lower() or
                        search_term in curr['country'].lower() or
                        search_term in curr['region'].lower()):
                        filtered_currencies.append(curr)
                    
                    # Check smart mappings
                    elif search_term in CURRENCY_SEARCH_MAPPINGS:
                        mapping = CURRENCY_SEARCH_MAPPINGS[search_term]
                        if any(term in curr['code'].lower() or term in curr['name'].lower() 
                              for term in mapping):
                            filtered_currencies.append(curr)
                
                currencies = filtered_currencies
            
            if limit:
                currencies = currencies[:limit]
            
            return currencies
        
        currencies = await cached_currency_list(filters, fetch_currencies)
        
        # Format response
        formatted_currencies = [{
            "code": curr["code"],
            "name": curr["name"],
            "country": curr["country"],
            "region": curr["region"],
            "priority": curr["priority"]
        } for curr in currencies]
        
        response_time = time.time() - start_time
        performance_monitor.record_request('/api/currencies', response_time, 200)
        
        return {
            "currencies": formatted_currencies,
            "count": len(formatted_currencies),
            "total_available": len(SUPPORTED_CURRENCIES),
            "filters_applied": {k: v for k, v in filters.items() if v is not None},
            "response_time_ms": round(response_time * 1000, 2),
            "timestamp": time.time()
        }
        
    except Exception as e:
        logger.error(f"Error fetching currencies: {e}")
        performance_monitor.record_request('/api/currencies', time.time() - start_time, 500)
        raise HTTPException(status_code=500, detail="Failed to fetch currencies")

@app.get("/api/regions")
@limiter.limit(f"{RATE_LIMIT_PER_MINUTE}/minute")
async def get_regions(request: Request):
    """Get all available regions with currency counts"""
    try:
        regions = {}
        for curr in SUPPORTED_CURRENCIES:
            region = curr['region']
            if region not in regions:
                regions[region] = {
                    'name': region,
                    'currency_count': 0,
                    'currencies': []
                }
            regions[region]['currency_count'] += 1
            regions[region]['currencies'].append({
                'code': curr['code'],
                'name': curr['name'],
                'country': curr['country']
            })
        
        return {
            'regions': list(regions.values()),
            'total_regions': len(regions),
            'timestamp': time.time()
        }
    except Exception as e:
        logger.error(f"Error fetching regions: {e}")
        raise HTTPException(status_code=500, detail="Failed to fetch regions")

@app.get("/api/search")
@limiter.limit(f"{RATE_LIMIT_PER_MINUTE}/minute")
async def search_currencies(
    request: Request,
    q: str = Query(..., description="Search query"),
    limit: int = Query(10, description="Maximum results to return")
):
    """Advanced currency search with intelligent matching"""
    try:
        start_time = time.time()
        query = q.lower().strip()
        
        if len(query) < 1:
            raise HTTPException(status_code=400, detail="Search query too short")
        
        results = []
        
        # Search through all currencies
        for curr in SUPPORTED_CURRENCIES:
            score = 0
            
            # Exact matches get highest score
            if query == curr['code'].lower():
                score += 100
            elif query == curr['name'].lower():
                score += 90
            elif query == curr['country'].lower():
                score += 85
            
            # Partial matches
            elif query in curr['code'].lower():
                score += 70
            elif query in curr['name'].lower():
                score += 60
            elif query in curr['country'].lower():
                score += 50
            elif query in curr['region'].lower():
                score += 40
            
            # Smart mapping matches
            elif query in CURRENCY_SEARCH_MAPPINGS:
                mapping = CURRENCY_SEARCH_MAPPINGS[query]
                if any(term in curr['code'].lower() or term in curr['name'].lower() 
                      for term in mapping):
                    score += 80
            
            if score > 0:
                results.append({
                    'currency': {
                        'code': curr['code'],
                        'name': curr['name'],
                        'country': curr['country'],
                        'region': curr['region'],
                        'priority': curr['priority']
                    },
                    'score': score,
                    'match_type': 'exact' if score >= 85 else 'partial' if score >= 50 else 'smart'
                })
        
        # Sort by score and limit results
        results.sort(key=lambda x: (-x['score'], x['currency']['priority']))
        results = results[:limit]
        
        response_time = time.time() - start_time
        performance_monitor.record_request('/api/search', response_time, 200)
        
        return {
            'query': q,
            'results': results,
            'count': len(results),
            'response_time_ms': round(response_time * 1000, 2),
            'timestamp': time.time()
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in currency search: {e}")
        raise HTTPException(status_code=500, detail="Search failed")

@app.get("/api/stats")
@limiter.limit("10/minute")
async def get_api_stats(request: Request):
    """Get comprehensive API performance statistics"""
    try:
        cache_stats = CacheMonitor.get_comprehensive_stats()
        perf_stats = performance_monitor.get_performance_summary()
        
        return {
            'api_info': {
                'version': '2.0.0',
                'currencies_supported': len(SUPPORTED_CURRENCIES),
                'regions_covered': len(set(curr['region'] for curr in SUPPORTED_CURRENCIES))
            },
            'cache_performance': cache_stats,
            'request_performance': perf_stats,
            'timestamp': time.time()
        }
    except Exception as e:
        logger.error(f"Error fetching stats: {e}")
        raise HTTPException(status_code=500, detail="Failed to fetch statistics")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)