#!/usr/bin/env python3
"""
Main entry point for Currency Converter API
Simple ASGI app export for deployment platforms like Zeabur
"""

from dotenv import load_dotenv

# Load environment variables first
load_dotenv()

# Import the optimized FastAPI app
from main_optimized import app

# Export for ASGI servers (required by Zeabur, Gunicorn, etc.)
application = app

# Also export as 'app' for compatibility
__all__ = ['app', 'application']
