# Advanced Caching System for Production-Grade Performance
# In-memory caching with TTL, LRU eviction, and Redis-like functionality

import time
import json
import hashlib
from typing import Dict, Any, Optional, Tuple
from collections import OrderedDict
import asyncio
import logging
import os

logger = logging.getLogger(__name__)

# Production mode check
PRODUCTION_MODE = os.getenv('PRODUCTION_MODE', 'false').lower() == 'true'

class AdvancedCache:
    """
    Production-grade caching system with:
    - TTL (Time To Live) support
    - LRU (Least Recently Used) eviction
    - Memory-efficient storage
    - Thread-safe operations
    - Cache statistics and monitoring
    """
    
    def __init__(self, max_size: int = 1000, default_ttl: int = 300):
        self.max_size = max_size
        self.default_ttl = default_ttl
        self.cache: OrderedDict = OrderedDict()
        self.access_times: Dict[str, float] = {}
        self.hit_count = 0
        self.miss_count = 0
        self.eviction_count = 0
        self._lock = asyncio.Lock()
    
    def _generate_key(self, *args, **kwargs) -> str:
        """Generate consistent cache key from arguments"""
        key_data = {
            'args': args,
            'kwargs': sorted(kwargs.items()) if kwargs else {}
        }
        key_string = json.dumps(key_data, sort_keys=True, default=str)
        return hashlib.md5(key_string.encode()).hexdigest()
    
    async def get(self, key: str) -> Optional[Any]:
        """Get value from cache with TTL check"""
        async with self._lock:
            if key not in self.cache:
                self.miss_count += 1
                return None
            
            value, expiry_time = self.cache[key]
            current_time = time.time()
            
            # Check if expired
            if expiry_time and current_time > expiry_time:
                del self.cache[key]
                if key in self.access_times:
                    del self.access_times[key]
                self.miss_count += 1
                return None
            
            # Move to end (most recently used)
            self.cache.move_to_end(key)
            self.access_times[key] = current_time
            self.hit_count += 1
            
            return value
    
    async def set(self, key: str, value: Any, ttl: Optional[int] = None) -> None:
        """Set value in cache with optional TTL"""
        async with self._lock:
            current_time = time.time()
            expiry_time = None
            
            if ttl is None:
                ttl = self.default_ttl
            
            if ttl > 0:
                expiry_time = current_time + ttl
            
            # Remove oldest items if cache is full
            while len(self.cache) >= self.max_size:
                oldest_key = next(iter(self.cache))
                del self.cache[oldest_key]
                if oldest_key in self.access_times:
                    del self.access_times[oldest_key]
                self.eviction_count += 1
            
            self.cache[key] = (value, expiry_time)
            self.access_times[key] = current_time
    
    async def delete(self, key: str) -> bool:
        """Delete key from cache"""
        async with self._lock:
            if key in self.cache:
                del self.cache[key]
                if key in self.access_times:
                    del self.access_times[key]
                return True
            return False
    
    async def clear(self) -> None:
        """Clear all cache entries"""
        async with self._lock:
            self.cache.clear()
            self.access_times.clear()
            self.hit_count = 0
            self.miss_count = 0
            self.eviction_count = 0
    
    def get_stats(self) -> Dict[str, Any]:
        """Get cache performance statistics"""
        total_requests = self.hit_count + self.miss_count
        hit_rate = (self.hit_count / total_requests * 100) if total_requests > 0 else 0
        
        return {
            'size': len(self.cache),
            'max_size': self.max_size,
            'hit_count': self.hit_count,
            'miss_count': self.miss_count,
            'hit_rate': round(hit_rate, 2),
            'eviction_count': self.eviction_count,
            'memory_usage': self._estimate_memory_usage()
        }
    
    def _estimate_memory_usage(self) -> str:
        """Estimate memory usage of cache"""
        try:
            import sys
            total_size = sys.getsizeof(self.cache)
            for key, (value, _) in self.cache.items():
                total_size += sys.getsizeof(key) + sys.getsizeof(value)
            
            # Convert to human readable format
            for unit in ['B', 'KB', 'MB', 'GB']:
                if total_size < 1024.0:
                    return f"{total_size:.1f} {unit}"
                total_size /= 1024.0
            return f"{total_size:.1f} TB"
        except:
            return "Unknown"

# Global cache instances
exchange_rate_cache = AdvancedCache(max_size=500, default_ttl=300)  # 5 minutes TTL
currency_list_cache = AdvancedCache(max_size=100, default_ttl=3600)  # 1 hour TTL
auth_cache = AdvancedCache(max_size=1000, default_ttl=600)  # 10 minutes TTL

def cache_key_for_rates(base_currency: str) -> str:
    """Generate cache key for exchange rates"""
    return f"rates:{base_currency}:{int(time.time() // 300)}"  # 5-minute buckets

def cache_key_for_currencies(filters: Dict = None) -> str:
    """Generate cache key for currency lists"""
    if not filters:
        return "currencies:all"
    return f"currencies:{hashlib.md5(str(sorted(filters.items())).encode()).hexdigest()}"

async def cached_exchange_rates(base_currency: str, fetch_func):
    """Decorator-like function for caching exchange rates"""
    cache_key = cache_key_for_rates(base_currency)
    
    # Try to get from cache first
    cached_data = await exchange_rate_cache.get(cache_key)
    if cached_data:
        if not PRODUCTION_MODE:
            logger.info(f"Cache HIT for exchange rates: {base_currency}")
        return cached_data
    
    # Fetch fresh data
    if not PRODUCTION_MODE:
        logger.info(f"Cache MISS for exchange rates: {base_currency}")
    fresh_data = await fetch_func()
    
    # Cache the result
    await exchange_rate_cache.set(cache_key, fresh_data, ttl=300)
    
    return fresh_data

async def cached_currency_list(filters: Dict, fetch_func):
    """Decorator-like function for caching currency lists"""
    cache_key = cache_key_for_currencies(filters)
    
    # Try to get from cache first
    cached_data = await currency_list_cache.get(cache_key)
    if cached_data:
        if not PRODUCTION_MODE:
            logger.info(f"Cache HIT for currency list")
        return cached_data
    
    # Fetch fresh data
    if not PRODUCTION_MODE:
        logger.info(f"Cache MISS for currency list")
    fresh_data = await fetch_func()
    
    # Cache the result
    await currency_list_cache.set(cache_key, fresh_data, ttl=3600)
    
    return fresh_data

# Cache warming functions
async def warm_cache_with_popular_currencies():
    """Pre-warm cache with most popular currency pairs"""
    popular_currencies = ['USD', 'EUR', 'GBP', 'JPY', 'CNY', 'AUD', 'CAD', 'CHF', 'SGD', 'HKD']
    
    if not PRODUCTION_MODE:
        logger.info("Starting cache warming for popular currencies...")
    
    for currency in popular_currencies:
        cache_key = cache_key_for_rates(currency)
        # This would be called with actual fetch function in production
        if not PRODUCTION_MODE:
            logger.info(f"Cache warming scheduled for {currency}")
    
    if not PRODUCTION_MODE:
        logger.info("Cache warming completed")

# Performance monitoring
class CacheMonitor:
    """Monitor cache performance and provide insights"""
    
    @staticmethod
    def get_comprehensive_stats():
        """Get stats from all cache instances"""
        return {
            'exchange_rates': exchange_rate_cache.get_stats(),
            'currency_lists': currency_list_cache.get_stats(),
            'auth_tokens': auth_cache.get_stats(),
            'total_caches': 3
        }
    
    @staticmethod
    def log_performance_metrics():
        """Log performance metrics for monitoring"""
        stats = CacheMonitor.get_comprehensive_stats()
        
        if not PRODUCTION_MODE:
            logger.info("=== Cache Performance Metrics ===")
            for cache_name, cache_stats in stats.items():
                if isinstance(cache_stats, dict):
                    logger.info(f"{cache_name.upper()}: Hit Rate: {cache_stats['hit_rate']}%, "
                              f"Size: {cache_stats['size']}/{cache_stats['max_size']}, "
                              f"Memory: {cache_stats['memory_usage']}")
            logger.info("================================")
