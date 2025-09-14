# Production-Grade Performance Optimizer
# Connection pooling, async optimizations, and request batching

import asyncio
import httpx
import time
from typing import List, Dict, Any, Optional
from contextlib import asynccontextmanager
import logging
from concurrent.futures import ThreadPoolExecutor
import json

logger = logging.getLogger(__name__)

class HTTPConnectionManager:
    """
    Advanced HTTP connection manager with:
    - Connection pooling
    - Automatic retries with exponential backoff
    - Request batching
    - Circuit breaker pattern
    - Performance monitoring
    """
    
    def __init__(self, max_connections: int = 100, max_keepalive: int = 20):
        self.limits = httpx.Limits(
            max_connections=max_connections,
            max_keepalive_connections=max_keepalive
        )
        self.timeout = httpx.Timeout(10.0, connect=5.0)
        self.client: Optional[httpx.AsyncClient] = None
        self.request_count = 0
        self.error_count = 0
        self.total_response_time = 0.0
        self.circuit_breaker_open = False
        self.last_failure_time = 0
        self.failure_threshold = 5
        self.recovery_timeout = 60  # seconds
    
    async def __aenter__(self):
        self.client = httpx.AsyncClient(
            limits=self.limits,
            timeout=self.timeout,
            http2=True,  # Enable HTTP/2 for better performance
            verify=True
        )
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        if self.client:
            await self.client.aclose()
    
    async def get_with_retry(self, url: str, headers: Dict = None, max_retries: int = 3) -> Optional[Dict]:
        """HTTP GET with exponential backoff retry logic"""
        if self._is_circuit_breaker_open():
            logger.warning("Circuit breaker is open, skipping request")
            return None
        
        for attempt in range(max_retries + 1):
            try:
                start_time = time.time()
                
                response = await self.client.get(url, headers=headers or {})
                
                response_time = time.time() - start_time
                self.total_response_time += response_time
                self.request_count += 1
                
                if response.status_code == 200:
                    self._record_success()
                    return response.json()
                elif response.status_code == 429:  # Rate limited
                    wait_time = min(2 ** attempt, 60)  # Max 60 seconds
                    logger.warning(f"Rate limited, waiting {wait_time}s before retry")
                    await asyncio.sleep(wait_time)
                    continue
                else:
                    logger.error(f"HTTP {response.status_code}: {response.text}")
                    self._record_failure()
                    
            except httpx.TimeoutException:
                wait_time = min(2 ** attempt, 30)
                logger.warning(f"Timeout on attempt {attempt + 1}, retrying in {wait_time}s")
                await asyncio.sleep(wait_time)
                self._record_failure()
                
            except Exception as e:
                logger.error(f"Request failed on attempt {attempt + 1}: {str(e)}")
                self._record_failure()
                if attempt < max_retries:
                    await asyncio.sleep(2 ** attempt)
        
        return None
    
    def _is_circuit_breaker_open(self) -> bool:
        """Check if circuit breaker is open"""
        if not self.circuit_breaker_open:
            return False
        
        # Check if recovery timeout has passed
        if time.time() - self.last_failure_time > self.recovery_timeout:
            self.circuit_breaker_open = False
            logger.info("Circuit breaker closed, resuming requests")
            return False
        
        return True
    
    def _record_success(self):
        """Record successful request"""
        if self.circuit_breaker_open:
            self.circuit_breaker_open = False
            logger.info("Circuit breaker closed after successful request")
    
    def _record_failure(self):
        """Record failed request and potentially open circuit breaker"""
        self.error_count += 1
        self.last_failure_time = time.time()
        
        recent_error_rate = self.error_count / max(self.request_count, 1)
        if recent_error_rate > 0.5 and self.error_count >= self.failure_threshold:
            self.circuit_breaker_open = True
            logger.warning("Circuit breaker opened due to high error rate")
    
    def get_performance_stats(self) -> Dict[str, Any]:
        """Get performance statistics"""
        avg_response_time = (
            self.total_response_time / self.request_count 
            if self.request_count > 0 else 0
        )
        
        error_rate = (
            self.error_count / self.request_count * 100 
            if self.request_count > 0 else 0
        )
        
        return {
            'total_requests': self.request_count,
            'total_errors': self.error_count,
            'error_rate_percent': round(error_rate, 2),
            'avg_response_time_ms': round(avg_response_time * 1000, 2),
            'circuit_breaker_open': self.circuit_breaker_open
        }

class RequestBatcher:
    """
    Batch multiple API requests for improved performance
    """
    
    def __init__(self, batch_size: int = 10, batch_timeout: float = 0.1):
        self.batch_size = batch_size
        self.batch_timeout = batch_timeout
        self.pending_requests: List[Dict] = []
        self.batch_lock = asyncio.Lock()
    
    async def add_request(self, request_data: Dict) -> Any:
        """Add request to batch and return result when batch is processed"""
        async with self.batch_lock:
            future = asyncio.Future()
            request_item = {
                'data': request_data,
                'future': future,
                'timestamp': time.time()
            }
            
            self.pending_requests.append(request_item)
            
            # Process batch if it's full or timeout reached
            if (len(self.pending_requests) >= self.batch_size or 
                self._should_process_batch()):
                await self._process_batch()
        
        return await future
    
    def _should_process_batch(self) -> bool:
        """Check if batch should be processed based on timeout"""
        if not self.pending_requests:
            return False
        
        oldest_request = min(self.pending_requests, key=lambda x: x['timestamp'])
        return time.time() - oldest_request['timestamp'] > self.batch_timeout
    
    async def _process_batch(self):
        """Process all pending requests in batch"""
        if not self.pending_requests:
            return
        
        batch = self.pending_requests.copy()
        self.pending_requests.clear()
        
        logger.info(f"Processing batch of {len(batch)} requests")
        
        # Process all requests concurrently
        tasks = []
        for request_item in batch:
            task = asyncio.create_task(
                self._process_single_request(request_item)
            )
            tasks.append(task)
        
        await asyncio.gather(*tasks, return_exceptions=True)
    
    async def _process_single_request(self, request_item: Dict):
        """Process a single request from the batch"""
        try:
            # This would be replaced with actual request processing logic
            result = await self._mock_process_request(request_item['data'])
            request_item['future'].set_result(result)
        except Exception as e:
            request_item['future'].set_exception(e)
    
    async def _mock_process_request(self, request_data: Dict) -> Dict:
        """Mock request processing - replace with actual logic"""
        await asyncio.sleep(0.01)  # Simulate processing time
        return {'processed': True, 'data': request_data}

class PerformanceMonitor:
    """
    Monitor and log performance metrics
    """
    
    def __init__(self):
        self.request_times: List[float] = []
        self.error_counts: Dict[str, int] = {}
        self.endpoint_stats: Dict[str, Dict] = {}
    
    def record_request(self, endpoint: str, response_time: float, status_code: int):
        """Record request performance metrics"""
        self.request_times.append(response_time)
        
        if endpoint not in self.endpoint_stats:
            self.endpoint_stats[endpoint] = {
                'count': 0,
                'total_time': 0,
                'errors': 0
            }
        
        stats = self.endpoint_stats[endpoint]
        stats['count'] += 1
        stats['total_time'] += response_time
        
        if status_code >= 400:
            stats['errors'] += 1
            error_key = f"{endpoint}:{status_code}"
            self.error_counts[error_key] = self.error_counts.get(error_key, 0) + 1
    
    def get_performance_summary(self) -> Dict[str, Any]:
        """Get comprehensive performance summary"""
        if not self.request_times:
            return {'status': 'No requests recorded'}
        
        avg_response_time = sum(self.request_times) / len(self.request_times)
        
        # Calculate percentiles
        sorted_times = sorted(self.request_times)
        p50 = sorted_times[len(sorted_times) // 2]
        p95 = sorted_times[int(len(sorted_times) * 0.95)]
        p99 = sorted_times[int(len(sorted_times) * 0.99)]
        
        endpoint_summary = {}
        for endpoint, stats in self.endpoint_stats.items():
            endpoint_summary[endpoint] = {
                'avg_response_time_ms': round(stats['total_time'] / stats['count'] * 1000, 2),
                'request_count': stats['count'],
                'error_rate_percent': round(stats['errors'] / stats['count'] * 100, 2)
            }
        
        return {
            'total_requests': len(self.request_times),
            'avg_response_time_ms': round(avg_response_time * 1000, 2),
            'p50_response_time_ms': round(p50 * 1000, 2),
            'p95_response_time_ms': round(p95 * 1000, 2),
            'p99_response_time_ms': round(p99 * 1000, 2),
            'total_errors': sum(self.error_counts.values()),
            'endpoint_stats': endpoint_summary,
            'error_breakdown': self.error_counts
        }

# Global instances
http_manager = HTTPConnectionManager()
request_batcher = RequestBatcher()
performance_monitor = PerformanceMonitor()

# Async context manager for HTTP connections
@asynccontextmanager
async def get_http_client():
    """Get optimized HTTP client with connection pooling"""
    async with HTTPConnectionManager() as client:
        yield client
