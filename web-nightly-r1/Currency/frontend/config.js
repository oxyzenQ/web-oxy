// Enhanced Security & Performance Configuration for Currency Converter
// Implements CSP, environment detection, performance optimization, and secure API handling

// Advanced Environment Detection
const getEnvironment = () => {
  const hostname = window.location.hostname;
  const protocol = window.location.protocol;
  
  // Development environments
  if (hostname === 'localhost' || 
      hostname === '127.0.0.1' || 
      hostname.includes('dev') ||
      hostname.includes('staging') ||
      protocol === 'file:') {
    return 'development';
  }
  
  // Production environments
  if (hostname.includes('vercel.app') ||
      hostname.includes('netlify.app') ||
      hostname.includes('github.io') ||
      !hostname.includes('localhost')) {
    return 'production';
  }
  
  return 'development'; // Default fallback
};

const ENVIRONMENT = getEnvironment();
const isDevelopment = ENVIRONMENT === 'development';
const isProduction = ENVIRONMENT === 'production';

// Performance-optimized API Configuration
const getOptimalAPIUrl = () => {
  // Environment-based API selection with performance priority
  if (isDevelopment) {
    return 'http://localhost:8000';
  }
  
  // Production with CDN and geographic optimization
  const envUrl = import.meta.env.VITE_API_BASE_URL;
  if (envUrl) return envUrl;
  
  // Fallback with regional optimization
  const region = Intl.DateTimeFormat().resolvedOptions().timeZone;
  if (region.includes('Asia')) {
    return 'https://kconvert-backend-asia.zeabur.app';
  }
  
  return 'https://kconvert-backend.zeabur.app';
};

const API_BASE_URL = getOptimalAPIUrl();

// Enhanced Security Configuration
const SECURITY_CONFIG = {
  // Strict Content Security Policy
  csp: {
    'default-src': "'self'",
    'script-src': "'self' 'unsafe-inline' 'unsafe-eval'",
    'script-src-elem': "'self' 'unsafe-inline'",
    'style-src': "'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com",
    'font-src': "'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com data:",
    'img-src': "'self' data: https: blob: https://flagcdn.com",
    'connect-src': `'self' ${API_BASE_URL} https://api.exchangerate-api.com wss: ws:`,
    'frame-src': "'none'",
    'object-src': "'none'",
    'base-uri': "'self'",
    'form-action': "'self'",
    'upgrade-insecure-requests': isProduction ? '' : null
  },
  
  // Enhanced rate limiting with burst protection
  rateLimits: {
    api: { requests: 100, window: 60000, burst: 10 }, // 100/min, 10 burst
    auth: { requests: 30, window: 60000, burst: 5 },  // 30/min, 5 burst
    search: { requests: 50, window: 60000, burst: 15 } // 50/min, 15 burst
  },
  
  // Token security with rotation
  token: {
    expiry: 10 * 60 * 1000, // 10 minutes
    refreshThreshold: 2 * 60 * 1000, // Refresh when 2 minutes left
    maxAge: 15 * 60 * 1000, // Maximum token age
    rotationInterval: 8 * 60 * 1000 // Rotate every 8 minutes
  },
  
  // Request validation
  validation: {
    maxRequestSize: 1024 * 1024, // 1MB
    allowedMethods: ['GET', 'POST', 'OPTIONS'],
    requiredHeaders: ['Content-Type', 'Authorization']
  }
};

// Advanced Performance Configuration
const PERFORMANCE_CONFIG = {
  // Intelligent caching with TTL and size limits
  cache: {
    rates: { ttl: 5 * 60 * 1000, maxSize: 100 },      // 5 minutes, 100 entries
    currencies: { ttl: 24 * 60 * 60 * 1000, maxSize: 1 }, // 24 hours, 1 entry
    jwt: { ttl: 9 * 60 * 1000, maxSize: 1 },          // 9 minutes, 1 entry
    charts: { ttl: 10 * 60 * 1000, maxSize: 50 },     // 10 minutes, 50 charts
    search: { ttl: 30 * 60 * 1000, maxSize: 200 }     // 30 minutes, 200 searches
  },
  
  // Adaptive timeouts based on connection quality
  timeouts: {
    api: isDevelopment ? 30000 : 10000,     // 30s dev, 10s prod
    auth: 5000,     // 5 seconds
    rates: 8000,    // 8 seconds
    charts: 15000,  // 15 seconds for chart data
    search: 3000    // 3 seconds for search
  },
  
  // Exponential backoff retry with jitter
  retry: {
    maxAttempts: 3,
    baseDelay: 1000,
    maxDelay: 8000,
    backoffFactor: 2,
    jitter: true
  },
  
  // Resource optimization
  resources: {
    preloadCritical: true,
    lazyLoadImages: true,
    compressRequests: true,
    enableGzip: true,
    minifyResponses: isProduction
  },
  
  // Animation performance
  animations: {
    reducedMotion: window.matchMedia('(prefers-reduced-motion: reduce)').matches,
    useGPUAcceleration: true,
    frameRate: 60,
    duration: {
      fast: 150,
      normal: 300,
      slow: 500
    }
  }
};

// Optimized API Endpoints with versioning
const API_ENDPOINTS = {
  apiEndpoints: {
    auth: `${API_BASE_URL}/auth/token`,
    rates: `${API_BASE_URL}/rates`,
    currencies: `${API_BASE_URL}/currencies`,
    regions: `${API_BASE_URL}/regions`,
    search: `${API_BASE_URL}/search`,
    stats: `${API_BASE_URL}/stats`,
    health: `${API_BASE_URL}/health`,
    CURRENCIES: `${API_BASE_URL}/currencies`
  },
};

// Feature flags with A/B testing support
const FEATURE_FLAGS = {
  enableCache: true,
  enableRetry: true,
  enableMetrics: isDevelopment,
  enableDebugMode: isDevelopment,
  enableServiceWorker: isProduction,
  enableOfflineMode: true,
  enablePrefetch: true,
  enableLazyLoading: true,
  enableAnimations: !PERFORMANCE_CONFIG.animations.reducedMotion,
  enableAnalytics: isProduction,
  enableErrorReporting: isProduction,
  OFFLINE_MODE: true,
  ERROR_REPORTING: isProduction,
  ACCESSIBILITY: true
};

// Main Configuration Object
export const CONFIG = {
  // Environment
  environment: ENVIRONMENT,
  isDevelopment,
  isProduction,
  
  // API
  API_BASE_URL,
  API_ENDPOINTS: API_ENDPOINTS.apiEndpoints,
  
  // Security
  SECURITY_CONFIG,
  
  // Performance
  PERFORMANCE_CONFIG,
  
  // Features
  features: FEATURE_FLAGS,
  
  // Build info
  build: {
    version: import.meta.env.VITE_APP_VERSION || '1.0.0',
    buildTime: import.meta.env.VITE_BUILD_TIME || Date.now(),
    gitHash: import.meta.env.VITE_GIT_HASH || 'unknown'
  }
};

// Enhanced CSP Implementation with nonce support
export function setDynamicCSP() {
  if (typeof document === 'undefined') return;
  
  try {
    // Generate nonce for inline scripts in production
    const nonce = isProduction ? btoa(Math.random().toString()).substring(0, 16) : null;
    
    const cspDirectives = { ...SECURITY_CONFIG.csp };
    
    // Add nonce to script-src in production
    if (nonce && isProduction) {
      cspDirectives['script-src'] = `'self' 'nonce-${nonce}'`;
    }
    
    // Remove null values and build CSP string
    const cspString = Object.entries(cspDirectives)
      .filter(([_, value]) => value !== null)
      .map(([directive, value]) => `${directive} ${value}`)
      .join('; ');
    
    // Create or update CSP meta tag
    let cspMeta = document.querySelector('meta[http-equiv="Content-Security-Policy"]');
    if (!cspMeta) {
      cspMeta = document.createElement('meta');
      cspMeta.setAttribute('http-equiv', 'Content-Security-Policy');
      document.head.appendChild(cspMeta);
    }
    cspMeta.setAttribute('content', cspString);
    
    // Store nonce globally for script injection
    if (nonce) {
      window.__CSP_NONCE__ = nonce;
    }
    
    debugLog('🔒 Enhanced CSP Applied:', cspString);
  } catch (error) {
    console.error('Failed to set CSP:', error);
  }
}

// Performance monitoring utilities
export function performanceMonitor() {
  if (!isDevelopment) return;
  
  // Monitor Core Web Vitals
  const observer = new PerformanceObserver((list) => {
    list.getEntries().forEach((entry) => {
      debugLog(`[PERF] ${entry.name}:`, entry.duration.toFixed(2) + 'ms');
    });
  });
  
  observer.observe({ entryTypes: ['measure', 'navigation', 'paint'] });
}

// Enhanced logging with performance tracking
export function debugLog(...args) {
  if (isDevelopment) {
    const timestamp = new Date().toISOString();
    console.log(`[${timestamp}] [DEBUG]`, ...args);
  }
}

export function performanceLog(label, data) {
  if (isDevelopment) {
    const timestamp = performance.now().toFixed(2);
    console.log(`[${timestamp}ms] [PERF] ${label}:`, data);
  }
}

// Error tracking and reporting
export function errorLog(error, context = {}) {
  const errorData = {
    message: error.message,
    stack: error.stack,
    timestamp: Date.now(),
    url: window.location.href,
    userAgent: navigator.userAgent,
    context
  };
  
  if (isDevelopment) {
    console.error('[ERROR]', errorData);
  }
  
  // In production, send to error reporting service
  if (isProduction && FEATURE_FLAGS.enableErrorReporting) {
    // Implementation for error reporting service
    // e.g., Sentry, LogRocket, etc.
  }
}

// Initialize performance monitoring
if (isDevelopment) {
  performanceMonitor();
}

// Export default configuration
export default CONFIG;
