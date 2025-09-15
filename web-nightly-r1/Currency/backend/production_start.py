#!/usr/bin/env python3
"""
Production-optimized startup script for Currency Converter API
Configures uvicorn with maximum performance settings
"""

import os
import uvicorn
from dotenv import load_dotenv

# Load production environment
load_dotenv('.env.production')

if __name__ == "__main__":
    # Production-optimized uvicorn configuration
    uvicorn.run(
        "main_optimized:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", 8000)),
        workers=int(os.getenv("WORKERS", 4)),  # Multi-process for better performance
        loop="uvloop",  # Faster event loop
        http="httptools",  # Faster HTTP parser
        log_level="error",  # Minimal logging for production
        access_log=False,  # Disable access logs for performance
        server_header=False,  # Remove server header for security
        date_header=False,  # Remove date header for minimal overhead
        reload=False,  # Disable auto-reload in production
    )
