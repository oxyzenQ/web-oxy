# Currency Converter Backend API

FastAPI backend for the Currency Converter mobile app with Redis caching and real-time exchange rates.

## Features

- **Fast Currency Conversion**: Real-time exchange rates from exchangerate-api.com
- **Redis Caching**: 1-hour cache for optimal performance
- **160+ Currencies**: Support for all major world currencies
- **Error Handling**: Graceful fallbacks and error responses
- **CORS Support**: Ready for mobile app integration
- **API Documentation**: Auto-generated Swagger/OpenAPI docs

## Quick Start

### 1. Install Dependencies
```bash
pip install -r requirements.txt
```

### 2. Setup Environment
```bash
cp .env.example .env
# Edit .env with your configuration
```

### 3. Start Redis (Optional)
```bash
# Using Docker
docker run -d -p 6379:6379 redis:alpine

# Or install locally
sudo apt-get install redis-server
redis-server
```

### 4. Run the API
```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

The API will be available at:
- **API**: http://localhost:8000
- **Docs**: http://localhost:8000/docs
- **Health**: http://localhost:8000/health

## API Endpoints

### Core Endpoints
- `GET /` - API information
- `GET /health` - Health check
- `GET /api/v1/currencies` - List all supported currencies
- `POST /api/v1/convert` - Convert currency amounts
- `GET /api/v1/rates/{base_currency}` - Get all rates for base currency
- `GET /api/v1/rate/{from}/{to}` - Get single exchange rate

### Example Usage

**Convert Currency:**
```bash
curl -X POST "http://localhost:8000/api/v1/convert" \
  -H "Content-Type: application/json" \
  -d '{
    "from_currency": "USD",
    "to_currency": "EUR", 
    "amount": 100
  }'
```

**Get Exchange Rates:**
```bash
curl "http://localhost:8000/api/v1/rates/USD"
```

## Project Structure

```
backend/
├── app/
│   ├── api/           # API routes
│   ├── core/          # Configuration
│   ├── models/        # Pydantic models
│   ├── services/      # Business logic
│   └── main.py        # FastAPI app
├── requirements.txt   # Dependencies
└── .env.example      # Environment template
```

## Configuration

Key environment variables:
- `REDIS_HOST` - Redis server host (default: localhost)
- `REDIS_PORT` - Redis server port (default: 6379)
- `EXCHANGE_API_KEY` - ExchangeRate-API key
- `CACHE_TTL` - Cache time-to-live in seconds (default: 3600)
- `DEBUG` - Debug mode (default: true)

## Performance

- **Caching**: Redis caches exchange rates for 1 hour
- **Async**: Full async/await support for high concurrency
- **Fast**: Sub-100ms response times with cache hits
- **Reliable**: Graceful degradation when Redis is unavailable

## Development

```bash
# Install dev dependencies
pip install -r requirements.txt

# Run with auto-reload
uvicorn app.main:app --reload

# Run tests (when added)
pytest
```

---
*Part of the Currency Converter Mobile App project*
