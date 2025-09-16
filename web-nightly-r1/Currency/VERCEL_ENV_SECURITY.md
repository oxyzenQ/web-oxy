# Vercel Environment Variables Security Guide

## Overview
This guide explains how to safely configure environment variables in Vercel to avoid the "VITE_ exposes this value to the browser" warning while maintaining security best practices.

## Safe vs Unsafe Environment Variables

### ✅ Safe for Browser Exposure (VITE_ prefix)
These variables are safe to expose to the browser and should use the `VITE_` prefix:

```bash
# API endpoints (public URLs)
VITE_API_BASE_URL=https://kconvert-backend.zeabur.app

# App metadata (public information)
VITE_APP_NAME=Kconvert
VITE_APP_VERSION=Stellar-1.5
```

### ⚠️ Internal Configuration (No VITE_ prefix)
These variables contain configuration that should NOT be exposed to browsers:

```bash
# Build and deployment settings
BUILD_MODE=production
ENABLE_ANALYTICS=true
ENABLE_PWA=true

# Performance tuning
CACHE_DURATION=300000
REQUEST_TIMEOUT=10000
RETRY_ATTEMPTS=3

# Security settings
CSP_ENABLED=true
SECURE_HEADERS=true
```

## Vercel Environment Variable Configuration

### Step 1: Set Variables in Vercel Dashboard
1. Go to your Vercel project dashboard
2. Navigate to Settings → Environment Variables
3. Add only the safe variables with `VITE_` prefix:

```
VITE_API_BASE_URL = https://kconvert-backend.zeabur.app
VITE_APP_NAME = Kconvert
VITE_APP_VERSION = Stellar-1.5
```

### Step 2: Internal Variables (Build-time only)
For internal configuration, add without `VITE_` prefix:

```
BUILD_MODE = production
ENABLE_ANALYTICS = true
ENABLE_PWA = true
CACHE_DURATION = 300000
REQUEST_TIMEOUT = 10000
RETRY_ATTEMPTS = 3
CSP_ENABLED = true
SECURE_HEADERS = true
```

## Code Implementation

The frontend `config.js` has been updated to handle both types of variables:

```javascript
// Safe browser-exposed variables
BASE_URL: import.meta?.env?.VITE_API_BASE_URL || fallback

// Internal variables (not exposed to browser)
REQUEST_TIMEOUT: parseInt(import.meta?.env?.REQUEST_TIMEOUT) || 10000
ANALYTICS: import.meta?.env?.ENABLE_ANALYTICS === 'true' || false
```

## Security Benefits

1. **Reduced Attack Surface**: Only necessary public information is exposed to browsers
2. **Configuration Privacy**: Internal settings remain server-side only
3. **Vercel Compliance**: Eliminates security warnings about exposed variables
4. **Best Practices**: Follows Vite's security recommendations

## What Gets Exposed vs Hidden

### Exposed to Browser (VITE_ variables):
- API endpoint URLs (already public)
- App name and version (public metadata)
- Public configuration flags

### Hidden from Browser (non-VITE_ variables):
- Performance tuning parameters
- Security configuration flags
- Build-time settings
- Internal feature toggles

## Verification

After deployment, you can verify the security by:
1. Opening browser DevTools → Sources
2. Checking that only `VITE_` variables appear in the bundled code
3. Confirming internal variables are not visible in the client-side bundle

This approach ensures maximum security while maintaining functionality and eliminating Vercel's security warnings.
