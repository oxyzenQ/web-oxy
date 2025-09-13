# Currency Converter Frontend

Modern, responsive frontend for the Currency Converter application with secure JWT authentication.

## 🚀 Features

- **Real-time Currency Conversion**: Live exchange rates from 170+ currencies
- **Secure Authentication**: JWT token-based API authentication
- **Responsive Design**: Works on desktop, tablet, and mobile devices
- **Flag Integration**: Visual country flags for currency selection
- **Error Handling**: User-friendly error messages and fallbacks
- **Input Validation**: Amount limits and currency code validation

## 📁 Project Structure

```
frontend/
├── index.html          # Main HTML file
├── style.css           # Styles and responsive design
├── cc.js              # Main JavaScript logic
├── countrys.js        # Currency to country mapping
├── assets/            # Static assets
│   ├── currency-conversion.png
│   ├── darkbyte.jpg
│   └── zero.txt
└── README.md          # This file
```

## 🛠 Configuration

### API Configuration (cc.js)
```javascript
// For local development
const API_BASE_URL = "http://localhost:8000";

// For production
const API_BASE_URL = "https://your-backend.onrender.com";

// JWT Token (generate with backend/generate_token.py)
const JWT_TOKEN = "your-jwt-token-here";
```

## 🌐 Development Server

### Local Development
```bash
# Start frontend server
cd frontend/
python -m http.server 3000

# Access at: http://localhost:3000
```

### With Backend
```bash
# Terminal 1: Start backend
cd backend/
source venv/bin/activate
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# Terminal 2: Start frontend
cd frontend/
python -m http.server 3000
```

## 🚀 Deployment

### Vercel Deployment
```bash
cd frontend/
vercel --prod
```

### Netlify Deployment
```bash
cd frontend/
netlify deploy --prod --dir .
```

## 🔧 Token Management

### Generate New Token
```bash
cd ../backend/
python generate_token.py 60  # 60 minutes validity
```

### Update Frontend
1. Copy token from generator output
2. Update `JWT_TOKEN` in `cc.js`
3. Redeploy frontend

## 🎨 Customization

### Styling
- Edit `style.css` for visual customization
- Responsive breakpoints already configured
- CSS variables for easy theming

### Currency List
- Modify `countrys.js` to add/remove currencies
- Flag URLs use flagcdn.com service
- Automatic flag updates on currency selection

### Error Messages
- Customize error messages in `cc.js`
- Add new error handling scenarios
- Implement retry logic as needed

## 🔐 Security Features

- **JWT Authentication**: All API calls require valid JWT token
- **Token Validation**: Frontend validates token before API calls
- **Error Handling**: Secure error messages without exposing internals
- **HTTPS Ready**: Production deployment uses HTTPS

## 📱 Browser Support

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 🐛 Troubleshooting

### "JWT token not configured"
- Generate token: `cd backend && python generate_token.py`
- Update `JWT_TOKEN` in `cc.js`

### "Token expired"
- Generate new token with longer duration
- Update frontend configuration

### CORS Errors
- Ensure backend CORS settings include frontend domain
- Check API_BASE_URL configuration

### Network Errors
- Verify backend is running
- Check API endpoint URLs
- Confirm JWT token is valid

## 📊 Performance

- **Lightweight**: ~50KB total size
- **Fast Loading**: Optimized assets and minimal dependencies
- **Responsive**: Smooth animations and transitions
- **Efficient**: Minimal API calls and smart caching
