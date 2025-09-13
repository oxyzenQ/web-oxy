# Currency Converter API Backend

Secure FastAPI backend for currency conversion with JWT authentication and rate limiting.

## 🚀 Features

- **JWT Authentication**: Secure token-based authentication
- **Rate Limiting**: 30 requests per minute per IP
- **CORS Protection**: Restricted to authorized domains
- **Exchange Rate Proxy**: Secure proxy to ExchangeRate-API
- **Health Monitoring**: Built-in health check endpoints
- **Error Handling**: Comprehensive error responses

## 📁 Project Structure

```
backend/
├── main.py              # FastAPI application
├── generate_token.py    # JWT token generator
├── requirements.txt     # Python dependencies
├── .env.example        # Environment variables template
├── Dockerfile          # Docker configuration
├── render.yaml         # Render deployment config
└── README.md           # This file
```

## 🛠 Setup

### 1. Environment Setup

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your values
nano .env
```

Required environment variables:
- `JWT_SECRET`: Strong secret key for JWT signing
- `EXCHANGE_API_KEY`: Your ExchangeRate-API key

### 2. Install Dependencies

```bash
pip install -r requirements.txt
```

### 3. Generate JWT Token

```bash
python generate_token.py 60  # Generate token valid for 60 minutes
```

### 4. Run Development Server

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

## 🌐 API Endpoints

### Health Check
```
GET /
GET /api/health
```

### Exchange Rates
```
GET /api/rates/{base_currency}?token={jwt_token}
```

**Parameters:**
- `base_currency`: 3-letter currency code (e.g., USD, EUR)
- `token`: Valid JWT token (query parameter)

**Response:**
```json
{
  "success": true,
  "base_code": "USD",
  "conversion_rates": {
    "EUR": 0.85,
    "GBP": 0.73,
    "JPY": 110.0
  },
  "time_last_update": "2024-01-01T00:00:00Z",
  "time_next_update": "2024-01-02T00:00:00Z"
}
```

## 🔐 Security Features

### JWT Authentication
- **Algorithm**: HS256
- **Expiration**: Configurable (default 10 minutes)
- **Owner Validation**: Only "kirai" owner allowed
- **Automatic Expiry**: Tokens expire automatically

### Rate Limiting
- **Limit**: 30 requests per minute per IP
- **Scope**: Per endpoint basis
- **Response**: HTTP 429 when exceeded

### CORS Protection
- **Allowed Origins**: Configurable whitelist
- **Methods**: GET, POST only
- **Credentials**: Supported for authenticated requests

## 🚀 Deployment

### Render.com Deployment

1. **Connect Repository**: Link your GitHub repo to Render
2. **Environment Variables**: Set in Render dashboard
   - `JWT_SECRET`: Your secure JWT secret
   - `EXCHANGE_API_KEY`: Your ExchangeRate-API key
3. **Deploy**: Render will automatically build and deploy

### Docker Deployment

```bash
# Build image
docker build -t currency-api .

# Run container
docker run -p 8000:8000 \
  -e JWT_SECRET="your-secret" \
  -e EXCHANGE_API_KEY="your-api-key" \
  currency-api
```

## 🔧 Token Management

### Generate New Token
```bash
python generate_token.py [duration_minutes]
```

### Token Usage in Frontend
```javascript
const JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...";
const response = await fetch(`${API_BASE_URL}/api/rates/USD?token=${JWT_TOKEN}`);
```

## 📊 Monitoring

### Health Check
```bash
curl https://your-backend.onrender.com/api/health
```

### Logs
- **Info**: Successful requests
- **Warning**: JWT verification failures
- **Error**: API errors and timeouts

## 🛡 Security Best Practices

1. **JWT Secret**: Use strong, random secret (32+ characters)
2. **Token Expiry**: Keep tokens short-lived (10-60 minutes)
3. **HTTPS Only**: Always use HTTPS in production
4. **Rate Limiting**: Monitor and adjust limits as needed
5. **CORS**: Restrict to specific domains only

## 🐛 Troubleshooting

### Common Issues

**Token Expired (401)**
```bash
# Generate new token
python generate_token.py
```

**Invalid Token (403)**
- Check JWT_SECRET matches between generator and API
- Verify token format and owner field

**Rate Limited (429)**
- Wait 1 minute before retrying
- Consider increasing rate limits if needed

**API Unavailable (502)**
- Check EXCHANGE_API_KEY is valid
- Verify ExchangeRate-API service status

## 📝 Development

### Local Testing
```bash
# Start server
uvicorn main:app --reload

# Test health
curl http://localhost:8000/api/health

# Test with token
curl "http://localhost:8000/api/rates/USD?token=YOUR_TOKEN"
```

### Adding New Features
1. Update `main.py` with new endpoints
2. Add tests if needed
3. Update documentation
4. Deploy to Render

## 📄 License

This project is part of the Currency Converter web application.
