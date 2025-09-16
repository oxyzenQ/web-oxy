#!/usr/bin/env python3
"""
Kconvert - Production Server Starter

Copyright (c) 2025 Team 6
All rights reserved.
"""
"""
Production-optimized startup script for Currency Converter API
Configures uvicorn with maximum performance settings
"""

import os
from dotenv import load_dotenv

# Load production environment
load_dotenv()

# Import the FastAPI app for ASGI
from main_optimized import app

# Export app for ASGI servers (Zeabur, Gunicorn, etc.)
application = app

if __name__ == "__main__":
    import uvicorn
    # Production-optimized uvicorn configuration
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=int(os.getenv("PORT", 8000)),
        workers=1,  # Single worker for Zeabur
        log_level="info",  # Better logging for debugging
        access_log=True,  # Enable access logs for monitoring
        reload=False,  # Disable auto-reload in production
    )
