# 🌍 Currency Converter - Hybrid Architecture

Secure, modern currency converter with JWT authentication and real-time exchange rates.

## 🏗 Project Structure

```
Currency/
├── frontend/              # Frontend application
│   ├── index.html        # Main HTML file
│   ├── style.css         # Styles and responsive design
│   ├── cc.js             # Main JavaScript logic
│   ├── countrys.js       # Currency to country mapping
│   ├── assets/           # Static assets
│   └── README.md         # Frontend documentation
├── backend/              # Backend API
│   ├── main.py           # FastAPI application
│   ├── generate_token.py # JWT token generator
│   ├── requirements.txt  # Python dependencies
│   ├── .env.example      # Environment template
│   ├── Dockerfile        # Container configuration
│   ├── render.yaml       # Render deployment config
│   └── README.md         # Backend documentation
├── .gitignore            # Enhanced git ignore rules
├── DEPLOYMENT_GUIDE.md   # Complete deployment guide
└── README.md             # This file
```

## 🚀 Quick Start

### Local Development

1. **Start Backend**:
   ```bash
   cd backend/
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   pip install -r requirements.txt
   cp .env.example .env      # Add your API keys
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```

2. **Generate JWT Token**:
   ```bash
   cd backend/
   python generate_token.py 30  # 30 minutes validity
   ```

3. **Start Frontend**:
   ```bash
   cd frontend/
   # Update cc.js with backend URL and JWT token
   python -m http.server 3000
   ```

4. **Access Application**:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8000
   - API Docs: http://localhost:8000/docs

## 🔐 Security Architecture

```
Frontend (Vercel) → JWT Auth → Backend (Render) → Exchange API
```

- **JWT Authentication**: Secure token-based API access
- **API Key Protection**: Exchange API key hidden in backend
- **Rate Limiting**: 30 requests/minute per IP
- **CORS Protection**: Domain-restricted access

## 🌐 Deployment

### Production Deployment

1. **Backend (Render)**:
   - Deploy backend to Render.com
   - Set environment variables: `JWT_SECRET`, `EXCHANGE_API_KEY`
   - Get backend URL: `https://your-app.onrender.com`

2. **Frontend (Vercel)**:
   - Update `cc.js` with production backend URL
   - Deploy to Vercel: `vercel --prod`
   - Get frontend URL: `https://your-app.vercel.app`

3. **Update CORS**:
   - Add Vercel URL to backend CORS origins
   - Redeploy backend

See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for detailed instructions.

## 🛠 Features

- **Real-time Exchange Rates**: 170+ currencies
- **Responsive Design**: Mobile-first approach
- **Flag Integration**: Visual country flags
- **Error Handling**: User-friendly messages
- **Input Validation**: Amount and currency validation
- **Security**: JWT authentication and rate limiting

## 🔧 Configuration

### Environment Variables

Backend (`.env`):
```env
JWT_SECRET=your-super-secret-jwt-key
EXCHANGE_API_KEY=your-exchangerate-api-key
ENVIRONMENT=production
```

Frontend (`cc.js`):
```javascript
const API_BASE_URL = "https://your-backend.onrender.com";
const JWT_TOKEN = "your-jwt-token-here";
```

## 📊 API Endpoints

- `GET /` - Health check
- `GET /api/health` - Detailed health status
- `GET /api/rates/{currency}?token={jwt}` - Exchange rates

## 🧪 Testing

```bash
# Test backend health
curl http://localhost:8000/api/health

# Test exchange rates
curl "http://localhost:8000/api/rates/USD?token=YOUR_JWT_TOKEN"
```

## 🔄 Token Management

Tokens expire automatically for security. To renew:

```bash
cd backend/
python generate_token.py 60  # 60 minutes
# Copy token to frontend cc.js
# Redeploy frontend
```

## 🐛 Troubleshooting

- **Token expired**: Generate new token and update frontend
- **CORS errors**: Check backend origins configuration
- **API errors**: Verify API key and backend status
- **Network issues**: Check backend URL and connectivity

## 📝 License

This project is part of the web-oxy repository.

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Make changes
4. Test locally
5. Submit pull request

---

**Security Note**: Never commit `.env` files or expose API keys in frontend code.
