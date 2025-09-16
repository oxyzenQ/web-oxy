#!/usr/bin/env python3
"""
Kconvert - Development Server Starter

Copyright (c) 2025 Team 6
All rights reserved.
"""
"""
Development startup script for Currency Converter API
Configures uvicorn with development-friendly settings
"""

import os
import uvicorn
from dotenv import load_dotenv

# Load development environment
load_dotenv('.env')

if __name__ == "__main__":
    # Development-optimized uvicorn configuration
    uvicorn.run(
        "main_optimized:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", 8000)),
        reload=True,  # Auto-reload on file changes
        log_level="info",  # Detailed logging for development
        access_log=True,  # Enable access logs for debugging
        workers=1,  # Single worker for development
    )
