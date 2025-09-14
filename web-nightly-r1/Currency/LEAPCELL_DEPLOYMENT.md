# Leapcell.io Deployment Guide for Currency Converter

## 🚀 Quick Deployment Steps

### 1. **Connect Repository**
- Go to [Leapcell.io](https://leapcell.io)
- Connect your GitHub repository
- Select the `Currency/backend` folder as the root directory

### 2. **Environment Variables**
Set these in Leapcell dashboard:

```bash
# Required Variables
JWT_SECRET=kconvert-super-secure-jwt-secret-2025-currency-converter-app-production
EXCHANGE_API_KEY=your-api-key-from-exchangerate-api.com
SERVERFRONTEND=https://your-kconvert-app.vercel.app

# Optional Variables
TOKEN_EXP_MINUTES=10
RATE_LIMIT_PER_MINUTE=30
AUTH_RATE_LIMIT_PER_MINUTE=20
PORT=8000
```

### 3. **Get Exchange Rate API Key**
1. Visit [exchangerate-api.com](https://exchangerate-api.com/)
2. Sign up for free (1,500 requests/month)
3. Copy your API key
4. Add it as `EXCHANGE_API_KEY` in Leapcell

### 4. **Deploy Configuration**
Leapcell will automatically detect:
- `requirements.txt` for dependencies
- `Procfile` for start command
- `runtime.txt` for Python version
- `leapcell.yaml` for advanced configuration

### 5. **Frontend Configuration**
After backend deployment, update your frontend:

```javascript
// In main.js CONFIG
const CONFIG = {
    API_BASE_URL: "https://your-app-name.leapcell.dev", // Replace with actual URL
    // ... other config
};
```

## 📋 **Leapcell Environment Variables Setup**

| Variable | Value | Purpose |
|----------|-------|---------|
| `JWT_SECRET` | `kconvert-super-secure-jwt-secret-2025-currency-converter-app-production` | JWT token signing |
| `EXCHANGE_API_KEY` | Your API key from exchangerate-api.com | Currency rate fetching |
| `SERVERFRONTEND` | `https://your-kconvert-app.vercel.app` | CORS allowed origin |
| `TOKEN_EXP_MINUTES` | `10` | JWT expiration time |
| `RATE_LIMIT_PER_MINUTE` | `30` | API rate limiting |

## 🔧 **Deployment Files Created**

- ✅ `leapcell.yaml` - Leapcell configuration
- ✅ `Procfile` - Start command
- ✅ `runtime.txt` - Python version
- ✅ Updated `.env.example` with Leapcell variables

## 🌐 **After Deployment**

1. **Test Backend**: Visit `https://your-app-name.leapcell.dev/`
2. **Test API**: `https://your-app-name.leapcell.dev/api/currencies`
3. **Update Frontend**: Change `API_BASE_URL` to your Leapcell URL
4. **Deploy Frontend**: Deploy to Vercel with updated backend URL

## 🎯 **Benefits of Leapcell**

- ✅ No credit card required
- ✅ Free tier available
- ✅ Automatic HTTPS
- ✅ Easy environment variable management
- ✅ Git-based deployment
- ✅ Built-in monitoring

Your intelligent searchable Currency Converter will work perfectly on Leapcell.io!
