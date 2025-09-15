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
    // Use environment variable or fallback to relative path for production
    BASE_URL: import.meta?.env?.VITE_API_BASE_URL || 
             (isProduction ? '/api' : 'http://localhost:8000'),
    
    // Timeout and retry settings
    REQUEST_TIMEOUT: 10000,
    RETRY_ATTEMPTS: 3,
    
    // Cache durations
    CACHE_DURATION: {
        CURRENCIES: 24 * 60 * 60 * 1000, // 24 hours
        EXCHANGE_RATES: 5 * 60 * 1000,   // 5 minutes
        JWT_TOKEN: 9 * 60 * 1000         // 9 minutes (token expires in 10)
    }
};

// CSP Configuration
const CSP_CONFIG = {
    production: {
        defaultSrc: "'self'",
        fontSrc: "'self' data: https://fonts.gstatic.com",
        styleSrc: "'self' 'unsafe-inline' https://fonts.googleapis.com",
        imgSrc: "'self' data: https://flagcdn.com",
        scriptSrc: "'self' 'unsafe-inline'",
        connectSrc: "'self'"
    },
    development: {
        defaultSrc: "'self'",
        fontSrc: "'self' data: https://fonts.gstatic.com",
        styleSrc: "'self' 'unsafe-inline' https://fonts.googleapis.com",
        imgSrc: "'self' data: https://flagcdn.com",
        scriptSrc: "'self' 'unsafe-inline' 'unsafe-eval'",
        connectSrc: "'self' http://localhost:8000 ws://localhost:*"
    }
};

// Feature flags
const FEATURES = {
    RATE_LIMITING_UI: true,
    ERROR_REPORTING: isProduction,
    DEBUG_LOGGING: isDevelopment,
    OFFLINE_MODE: true,
    PERFORMANCE_MONITORING: isDevelopment,
    ACCESSIBILITY: true
};

// Export configuration
export const CONFIG = {
    API_BASE_URL: API_CONFIG.BASE_URL,
    REQUEST_TIMEOUT: API_CONFIG.REQUEST_TIMEOUT,
    RETRY_ATTEMPTS: API_CONFIG.RETRY_ATTEMPTS,
    CURRENCY_UPDATE_INTERVAL: 60000,
    DEBUG_MODE: isDevelopment,
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
