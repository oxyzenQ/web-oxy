# 🚀 Hybrid Architecture Deployment Guide

Complete step-by-step guide for deploying the Currency Converter with secure JWT authentication.

## 📋 Architecture Overview

```
Frontend (Vercel) → Static HTML/CSS/JS
       ↓ JWT Auth
Backend (Render) → FastAPI + JWT + Exchange API Proxy
       ↓ Secure API Key
ExchangeRate-API → Real exchange data
```

## 🏗 Step 1: Backend Deployment (Render)

### 1.1 Prepare Repository
```bash
# Your backend files are ready in /backend/
# - main.py (FastAPI app)
# - requirements.txt (dependencies)
# - render.yaml (deployment config)
# - generate_token.py (JWT generator)
```

### 1.2 Deploy to Render
1. **Create Render Account**: Sign up at [render.com](https://render.com)
2. **Connect GitHub**: Link your repository
3. **Create Web Service**:
   - Repository: `your-username/web-oxy`
   - Branch: `main`
   - Root Directory: `Currency/backend`
   - Environment: `Python 3`
   - Build Command: `pip install -r requirements.txt`
   - Start Command: `uvicorn main:app --host 0.0.0.0 --port $PORT`

### 1.3 Set Environment Variables
In Render dashboard, add:
```
JWT_SECRET=your-super-secret-jwt-key-minimum-32-characters-long
EXCHANGE_API_KEY=your-exchangerate-api-key-from-exchangerate-api.com
ENVIRONMENT=production
```

### 1.4 Get Backend URL
After deployment: `https://your-app-name.onrender.com`

## 🎫 Step 2: Generate JWT Token

### 2.1 Local Token Generation
```bash
cd backend/
python generate_token.py 60  # 60 minutes validity
```

### 2.2 Copy Token Output
```
🔐 JWT Token Generated Successfully!
============================================================
📅 Valid for: 60 minutes
⏰ Expires at: 2024-01-01 15:30:00
============================================================
🎫 Your JWT Token:
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJvd25lciI6ImtpcmFpIiwiaWF0IjoxNzA0MTE2NDAwLCJleHAiOjE3MDQxMjAwMDAsInB1cnBvc2UiOiJjdXJyZW5jeV9hcGlfYWNjZXNzIn0.example-signature
============================================================
```

## 🌐 Step 3: Frontend Configuration

### 3.1 Update API Configuration
Edit `cc.js`:
```javascript
// Replace these values
const API_BASE_URL = "https://your-app-name.onrender.com";
const JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."; // Your actual token
```

### 3.2 Update CORS in Backend
Edit `backend/main.py`:
```python
origins = [
    "https://your-frontend-name.vercel.app",  # Your actual Vercel domain
    "http://localhost:3000",  # For local development
]
```

## 🚀 Step 4: Frontend Deployment (Vercel)

### 4.1 Deploy to Vercel
1. **Install Vercel CLI**: `npm i -g vercel`
2. **Deploy**: 
   ```bash
   cd Currency/  # Root directory with index.html
   vercel --prod
   ```
3. **Get URL**: `https://your-frontend-name.vercel.app`

### 4.2 Update Backend CORS
Update `backend/main.py` with your actual Vercel URL and redeploy Render service.

## 🔐 Step 5: Security Workflow

### 5.1 Token Management
```bash
# Generate new token (run when needed)
python backend/generate_token.py 60

# Update frontend with new token
# Edit cc.js → JWT_TOKEN = "new-token"
# Redeploy to Vercel
```

### 5.2 Security Features Active
- ✅ JWT Authentication (HS256)
- ✅ Rate Limiting (30 req/min per IP)
- ✅ CORS Protection (domain whitelist)
- ✅ API Key Hidden (backend only)
- ✅ Token Expiration (auto-expire)

## 🧪 Step 6: Testing

### 6.1 Test Backend Health
```bash
curl https://your-app-name.onrender.com/api/health
```

### 6.2 Test API with Token
```bash
curl "https://your-app-name.onrender.com/api/rates/USD?token=YOUR_JWT_TOKEN"
```

### 6.3 Test Frontend
1. Open: `https://your-frontend-name.vercel.app`
2. Try currency conversion
3. Check browser console for errors

## 🛠 Step 7: Maintenance Workflow

### 7.1 Token Renewal (Every 60 minutes)
```bash
# 1. Generate new token
python backend/generate_token.py 60

# 2. Update frontend
# Edit cc.js with new JWT_TOKEN

# 3. Redeploy frontend
vercel --prod
```

### 7.2 Automated Token Renewal (Optional)
Create GitHub Action for auto-token generation:

```yaml
# .github/workflows/token-renewal.yml
name: Token Renewal
on:
  schedule:
    - cron: '0 */1 * * *'  # Every hour
  workflow_dispatch:

jobs:
  renew-token:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Generate Token
        run: |
          cd backend
          python generate_token.py 120 > token.txt
          # Add logic to update Vercel environment variable
```

## 🚨 Security Checklist

- [ ] JWT_SECRET is strong (32+ characters)
- [ ] EXCHANGE_API_KEY is not exposed in frontend
- [ ] CORS origins are restricted to your domains
- [ ] Tokens have reasonable expiry (10-120 minutes)
- [ ] Rate limiting is active (30 req/min)
- [ ] HTTPS is enforced in production
- [ ] No sensitive data in git repository

## 🐛 Troubleshooting

### Frontend Shows "JWT token not configured"
- Run `python backend/generate_token.py`
- Copy token to `cc.js` → `JWT_TOKEN`
- Redeploy frontend

### "Token expired" Error
- Generate new token: `python backend/generate_token.py 60`
- Update frontend and redeploy

### "Invalid token" Error
- Check JWT_SECRET matches between backend and token generator
- Verify token format is correct

### CORS Error
- Update backend `origins` list with correct Vercel URL
- Redeploy backend to Render

### Rate Limited (429)
- Wait 1 minute before retrying
- Consider increasing rate limits in `main.py`

## 📊 Monitoring

### Backend Logs (Render)
- Check Render dashboard → Logs
- Monitor for JWT errors, rate limits, API failures

### Frontend Errors
- Browser DevTools → Console
- Network tab for API request status

## 🎯 Production URLs

After deployment, update these placeholders:
- **Backend**: `https://your-app-name.onrender.com`
- **Frontend**: `https://your-frontend-name.vercel.app`
- **Health Check**: `https://your-app-name.onrender.com/api/health`

## 🔄 Workflow Summary

1. **Deploy Backend** → Render (with environment variables)
2. **Generate Token** → `python generate_token.py 60`
3. **Update Frontend** → Add backend URL + JWT token
4. **Deploy Frontend** → Vercel
5. **Update CORS** → Add Vercel URL to backend origins
6. **Test End-to-End** → Verify currency conversion works
7. **Renew Tokens** → Every hour or as needed

Your secure hybrid architecture is now live! 🚀
