/*
 * Kconvert - Frontend Configuration
 * 
 * Copyright (c) 2025 Team 6
 * All rights reserved.
 */
// Configuration file for Currency Converter
// Handles environment-specific settings and API endpoints

// Environment detection
const isDevelopment = import.meta?.env?.MODE === 'development' || 
                     location.hostname === 'localhost' || 
                     location.hostname === '127.0.0.1';

const isProduction = import.meta?.env?.MODE === 'production' ||
                    location.hostname.includes('vercel.app') ||
                    location.hostname.includes('netlify.app') ||
                    location.hostname.includes('github.io');

// API Configuration
const API_CONFIG = {
    BASE_URL: 'https://kconvert-backend.zeabur.app',
    REQUEST_TIMEOUT: parseInt(import.meta?.env?.VITE_REQUEST_TIMEOUT) || 10000,
    RETRY_ATTEMPTS: parseInt(import.meta?.env?.VITE_RETRY_ATTEMPTS) || 3,
    CACHE_DURATION: parseInt(import.meta?.env?.VITE_CACHE_DURATION) || 300000
};

// CSP Configuration
const CSP_CONFIG = {
    production: {
        defaultSrc: "'self'",
        fontSrc: "'self' data:",
        styleSrc: "'self' 'unsafe-inline'",
        imgSrc: "'self' data: https://flagcdn.com",
        scriptSrc: "'self' 'unsafe-inline'",
        scriptSrcElem: "'self' 'unsafe-inline'",
        connectSrc: `'self' https://kconvert-backend.zeabur.app https:`,
        objectSrc: "'none'",
        baseUri: "'self'"
    },
    development: {
        defaultSrc: "'self'",
        fontSrc: "'self' data:",
        styleSrc: "'self' 'unsafe-inline'",
        imgSrc: "'self' data: https://flagcdn.com",
        scriptSrc: "'self' 'unsafe-inline' 'unsafe-eval'",
        connectSrc: "'self' http://localhost:8000 ws://localhost:*"
    }
};

// Feature flags - use env vars if available, otherwise defaults
const FEATURES = {
    RATE_LIMITING_UI: true,
    ERROR_REPORTING: isProduction,
    // Allow overriding debug via env: ENABLE_DEBUG=true/false
    DEBUG_LOGGING: (import.meta?.env?.ENABLE_DEBUG === 'true') ? true
                   : (import.meta?.env?.ENABLE_DEBUG === 'false') ? false
                   : isDevelopment,
    OFFLINE_MODE: true,
    PERFORMANCE_MONITORING: isDevelopment,
    ACCESSIBILITY: true,
    ANALYTICS: import.meta?.env?.ENABLE_ANALYTICS === 'true' || false,
    PWA: import.meta?.env?.ENABLE_PWA === 'true' || false,
    CSP: import.meta?.env?.CSP_ENABLED === 'true' || true,
    SECURE_HEADERS: import.meta?.env?.SECURE_HEADERS === 'true' || true
};

// Export configuration
export const CONFIG = {
    API_BASE_URL: API_CONFIG.BASE_URL,
    REQUEST_TIMEOUT: API_CONFIG.REQUEST_TIMEOUT,
    RETRY_ATTEMPTS: API_CONFIG.RETRY_ATTEMPTS,
    CURRENCY_UPDATE_INTERVAL: 60000,
    // DEBUG_MODE is aligned with DEBUG_LOGGING flag
    DEBUG_MODE: (import.meta?.env?.VITE_ENABLE_DEBUG === 'true') ? true
               : (import.meta?.env?.VITE_ENABLE_DEBUG === 'false') ? false
               : isDevelopment,
    IS_PRODUCTION: isProduction,
    CACHE_DURATION: API_CONFIG.CACHE_DURATION,
    FEATURES: FEATURES
};

export const CSP_SETTINGS = isDevelopment ? CSP_CONFIG.development : CSP_CONFIG.production;

// Set dynamic CSP based on environment
export function setDynamicCSP() {
    const cspSettings = CSP_SETTINGS;
    
    let cspContent = Object.entries(cspSettings)
        .map(([directive, value]) => {
            const kebabDirective = directive.replace(/([A-Z])/g, '-$1').toLowerCase();
            return `${kebabDirective} ${value}`;
        })
        .join('; ') + ';';
    
    const meta = document.createElement('meta');
    meta.httpEquiv = 'Content-Security-Policy';
    meta.content = cspContent;
    document.head.appendChild(meta);
}

// Environment info for debugging
if (isDevelopment) {
    console.log('🔧 Development mode active');
    console.log('📡 API Base URL:', CONFIG.API_BASE_URL);
    console.log('🛡️ CSP Settings:', CSP_SETTINGS);
}
