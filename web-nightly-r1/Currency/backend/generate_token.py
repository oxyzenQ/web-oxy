#!/usr/bin/env python3
"""
JWT Token Generator for Currency Converter API
Usage: python generate_token.py [duration_minutes]
"""

from jose import jwt
import time
import os
import sys
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

SECRET_KEY = os.getenv("JWT_SECRET", "your-super-secret-jwt-key-change-this")
ALGORITHM = "HS256"

def generate_token(duration_minutes=10):
    """
    Generate JWT token for API access
    
    Args:
        duration_minutes (int): Token validity duration in minutes (default: 10)
    
    Returns:
        str: JWT token
    """
    current_time = time.time()
    expiration_time = current_time + (duration_minutes * 60)
    
    payload = {
        "owner": "kirai",
        "iat": current_time,  # issued at
        "exp": expiration_time,  # expiration
        "purpose": "currency_api_access"
    }
    
    token = jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)
    return token

def main():
    """Main function to generate and display token"""
    # Get duration from command line argument
    duration = 10  # default 10 minutes
    
    if len(sys.argv) > 1:
        try:
            duration = int(sys.argv[1])
            if duration <= 0:
                print("❌ Duration must be positive number")
                sys.exit(1)
        except ValueError:
            print("❌ Invalid duration. Please provide a number.")
            sys.exit(1)
    
    # Check if JWT_SECRET is properly configured
    if SECRET_KEY == "your-super-secret-jwt-key-change-this":
        print("⚠️  WARNING: Using default JWT_SECRET. Please set a secure secret in .env file!")
    
    # Generate token
    token = generate_token(duration)
    
    # Display results
    print("🔐 JWT Token Generated Successfully!")
    print("=" * 60)
    print(f"📅 Valid for: {duration} minutes")
    print(f"⏰ Expires at: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time() + duration * 60))}")
    print("=" * 60)
    print("🎫 Your JWT Token:")
    print(token)
    print("=" * 60)
    print("\n📋 Usage Instructions:")
    print("1. Copy the token above")
    print("2. Use it in your frontend API calls:")
    print(f"   fetch('https://your-backend.onrender.com/api/rates/USD?token={token[:20]}...')")
    print("3. Or test with curl:")
    print(f"   curl 'https://your-backend.onrender.com/api/rates/USD?token={token[:20]}...'")
    print("\n⚠️  Keep this token secure and don't share it publicly!")

if __name__ == "__main__":
    main()
