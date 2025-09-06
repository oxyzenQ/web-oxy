# Currency Converter Mobile App

A modern mobile currency converter built with Kotlin Android + Python FastAPI backend.

## Project Structure

```
currency-mobile-app/
├── backend/           # Python FastAPI backend
│   ├── app/          # FastAPI application
│   ├── requirements.txt
│   └── README.md
├── android-app/      # Kotlin Android application
│   ├── app/         # Android app source
│   ├── build.gradle
│   └── README.md
├── docs/            # Documentation
└── README.md        # This file
```

## Features

### Backend (Python FastAPI)
- Real-time currency conversion API
- Redis caching for performance
- Rate limiting and error handling
- Historical rates support
- Multiple API source fallbacks

### Android App (Kotlin)
- Modern UI with Jetpack Compose
- Offline mode support
- Currency favorites
- Historical rate charts
- Dark/Light theme support
- Material Design 3

## Quick Start

### Backend Setup
```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload
```

### Android Development
Open `android-app` folder in Android Studio

## API Endpoints

- `GET /currencies` - List all supported currencies
- `POST /convert` - Convert currency amounts
- `GET /rates/{base_currency}` - Get all rates for base currency
- `GET /historical/{date}` - Historical rates (optional)

## Tech Stack

**Backend:**
- Python 3.11+
- FastAPI
- Redis
- Pydantic
- httpx

**Android:**
- Kotlin
- Jetpack Compose
- Retrofit
- Room Database
- Material Design 3

---
*Created by oxyzenQ - 2025*
