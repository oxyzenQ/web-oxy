/*
 * Kconvert - Frontend Main Application
 * Currency Converter - Consolidated Main JS File
 * All functionality in one file for easy maintenance
 * 
 * Copyright (c) 2025 Team 6
 * All rights reserved.
 */

// Import fonts and icons
import '@fontsource/inter/400.css';
import '@fontsource/inter/500.css';
import '@fontsource/inter/600.css';
import '@fortawesome/fontawesome-free/css/all.css';

// Import secure configuration
import { CONFIG, setDynamicCSP } from './config.js';

// Helper function to build API URLs correctly
function buildApiUrl(endpoint) {
    const apiBase = CONFIG.API_BASE_URL;
    
    // Debug logging
    if (CONFIG.DEBUG_MODE) {
        console.log('🔧 buildApiUrl debug:', { endpoint, apiBase, CONFIG });
    }
    
    if (!apiBase || apiBase === '/api') {
        return endpoint; // Use relative path for same-origin
    }
    
    // Remove leading slash from endpoint if present
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint;
    
    // If apiBase already contains /api, use it as-is, otherwise append /api
    const baseUrl = apiBase.includes('/api') ? apiBase : `${apiBase}/api`;
    
    try {
        const fullUrl = new URL(cleanEndpoint, `${baseUrl}/`).toString();
        if (CONFIG.DEBUG_MODE) {
            console.log('🌐 Built API URL:', fullUrl);
        }
        return fullUrl;
    } catch (e) {
        const base = String(baseUrl).replace(/\/$/, '');
        const fallbackUrl = `${base}/${cleanEndpoint}`;
        if (CONFIG.DEBUG_MODE) {
            console.log('🌐 Fallback API URL:', fallbackUrl);
        }
        return fallbackUrl;
    }
}

// Lazy-load chart functionality to reduce initial bundle size
let ChartAPI = null;
async function ensureChartLoaded() {
    if (ChartAPI) return ChartAPI;
    ChartAPI = await import('./chart.js');
    if (ChartAPI?.initializeExchangeChart) {
        ChartAPI.initializeExchangeChart();
    }
    return ChartAPI;
}

// Set CSP immediately
setDynamicCSP();

// Configuration is now imported from secure config file
window.APP_CONFIG = CONFIG;

// ===== ENHANCED STATE MANAGEMENT =====
// JWT token management with caching
class TokenManager {
    constructor() {
        this.token = null;
        this.tokenExpiry = null;
        this.refreshPromise = null;
    }

    isTokenValid() {
        return this.token && this.tokenExpiry && Date.now() < this.tokenExpiry;
    }

    async getValidToken() {
        if (this.isTokenValid()) {
            return this.token;
        }

        // Prevent multiple simultaneous token refreshes
        if (this.refreshPromise) {
            return await this.refreshPromise;
        }

        this.refreshPromise = this.fetchNewToken();
        try {
            const token = await this.refreshPromise;
            this.refreshPromise = null;
            return token;
        } catch (error) {
            this.refreshPromise = null;
            throw error;
        }
    }

    async fetchNewToken() {
        const response = await fetchWithRetry(buildApiUrl('auth'), {
            method: 'GET',
            cache: 'no-store'
        });
        
        if (!response.ok) {
            throw new Error(`Auth failed: ${response.status}`);
        }
        
        const data = await response.json();
        if (!data?.token) {
            throw new Error('No token in auth response');
        }
        
        this.token = String(data.token);
        this.tokenExpiry = Date.now() + CONFIG.CACHE_DURATION.JWT_TOKEN;
        
        if (CONFIG.DEBUG_MODE) {
            console.log('🔑 JWT token refreshed, expires in 9 minutes');
        }
        
        return this.token;
    }

    clearToken() {
        this.token = null;
        this.tokenExpiry = null;
        this.refreshPromise = null;
    }
}

const tokenManager = new TokenManager();

// Enhanced caching system
class CacheManager {
    constructor() {
        this.cache = new Map();
        this.cacheTimestamps = new Map();
    }

    set(key, value, duration = CONFIG.CACHE_DURATION.EXCHANGE_RATES) {
        this.cache.set(key, value);
        this.cacheTimestamps.set(key, Date.now() + duration);
        
        if (CONFIG.DEBUG_MODE) {
            console.log(`💾 Cached: ${key} (expires in ${Math.round(duration/1000)}s)`);
        }
    }

    get(key) {
        const expiry = this.cacheTimestamps.get(key);
        if (!expiry || Date.now() > expiry) {
            this.delete(key);
            return null;
        }
        return this.cache.get(key);
    }

    delete(key) {
        this.cache.delete(key);
        this.cacheTimestamps.delete(key);
    }

    clear() {
        this.cache.clear();
        this.cacheTimestamps.clear();
    }

    getStats() {
        return {
            size: this.cache.size,
            keys: Array.from(this.cache.keys())
        };
    }
}

const cacheManager = new CacheManager();

// Performance monitoring
class PerformanceMonitor {
    constructor() {
        this.metrics = {
            apiCalls: 0,
            cacheHits: 0,
            cacheMisses: 0,
            errors: 0,
            averageResponseTime: 0,
            totalResponseTime: 0
        };
    }

    recordApiCall(responseTime, fromCache = false) {
        this.metrics.apiCalls++;
        if (fromCache) {
            this.metrics.cacheHits++;
        } else {
            this.metrics.cacheMisses++;
            this.metrics.totalResponseTime += responseTime;
            this.metrics.averageResponseTime = this.metrics.totalResponseTime / this.metrics.cacheMisses;
        }
    }

    recordError() {
        this.metrics.errors++;
    }

    getStats() {
        return {
            ...this.metrics,
            cacheHitRate: this.metrics.apiCalls > 0 ? 
                (this.metrics.cacheHits / this.metrics.apiCalls * 100).toFixed(1) + '%' : '0%'
        };
    }

    reset() {
        Object.keys(this.metrics).forEach(key => {
            this.metrics[key] = 0;
        });
    }
}

const performanceMonitor = new PerformanceMonitor();

// Legacy compatibility
let jwtToken = null;

// ===== CURRENCY & COUNTRY DATA =====
// Centralized currency data with consistent country mappings
const SUPPORTED_CURRENCIES = [
    { code: 'USD', name: 'US Dollar', country: 'us' },
    { code: 'EUR', name: 'Euro', country: 'fr' },
    { code: 'GBP', name: 'British Pound', country: 'gb' },
    { code: 'JPY', name: 'Japanese Yen', country: 'jp' },
    { code: 'AUD', name: 'Australian Dollar', country: 'au' },
    { code: 'CAD', name: 'Canadian Dollar', country: 'ca' },
    { code: 'CHF', name: 'Swiss Franc', country: 'ch' },
    { code: 'CNY', name: 'Chinese Yuan', country: 'cn' },
    { code: 'SEK', name: 'Swedish Krona', country: 'se' },
    { code: 'NZD', name: 'New Zealand Dollar', country: 'nz' },
    { code: 'MXN', name: 'Mexican Peso', country: 'mx' },
    { code: 'SGD', name: 'Singapore Dollar', country: 'sg' },
    { code: 'HKD', name: 'Hong Kong Dollar', country: 'hk' },
    { code: 'NOK', name: 'Norwegian Krone', country: 'no' },
    { code: 'KRW', name: 'South Korean Won', country: 'kr' },
    { code: 'TRY', name: 'Turkish Lira', country: 'tr' },
    { code: 'RUB', name: 'Russian Ruble', country: 'ru' },
    { code: 'INR', name: 'Indian Rupee', country: 'in' },
    { code: 'BRL', name: 'Brazilian Real', country: 'br' },
    { code: 'ZAR', name: 'South African Rand', country: 'za' },
    { code: 'DKK', name: 'Danish Krone', country: 'dk' },
    { code: 'PLN', name: 'Polish Zloty', country: 'pl' },
    { code: 'TWD', name: 'Taiwan Dollar', country: 'tw' },
    { code: 'THB', name: 'Thai Baht', country: 'th' },
    { code: 'IDR', name: 'Indonesian Rupiah', country: 'id' },
    { code: 'HUF', name: 'Hungarian Forint', country: 'hu' },
    { code: 'CZK', name: 'Czech Koruna', country: 'cz' },
    { code: 'ILS', name: 'Israeli Shekel', country: 'il' },
    { code: 'CLP', name: 'Chilean Peso', country: 'cl' },
    { code: 'PHP', name: 'Philippine Peso', country: 'ph' },
    { code: 'AED', name: 'UAE Dirham', country: 'ae' },
    { code: 'COP', name: 'Colombian Peso', country: 'co' },
    { code: 'SAR', name: 'Saudi Riyal', country: 'sa' },
    { code: 'MYR', name: 'Malaysian Ringgit', country: 'my' },
    { code: 'RON', name: 'Romanian Leu', country: 'ro' }
];

// Get country code from currency code
function getCountryFromCurrency(currencyCode) {
    const currency = SUPPORTED_CURRENCIES.find(c => c.code === currencyCode);
    return currency?.country || currencyCode.slice(0, 2).toLowerCase();
}

// ===== INTELLIGENT SEARCH ENGINE =====
// Advanced fuzzy search with ML-like pattern matching
function calculateSearchScore(currency, query) {
    if (!query) return 0;
    
    const q = query.toLowerCase().trim();
    const code = currency.code.toLowerCase();
    const name = currency.name.toLowerCase();
    const country = currency.country.toLowerCase();
    
    let score = 0;
    
    // Exact matches get highest priority
    if (code === q) score += 100;
    if (name === q) score += 90;
    if (country === q) score += 85;
    
    // Starts with matches
    if (code.startsWith(q)) score += 80;
    if (name.startsWith(q)) score += 70;
    if (country.startsWith(q)) score += 65;
    
    // Contains matches
    if (code.includes(q)) score += 60;
    if (name.includes(q)) score += 50;
    if (country.includes(q)) score += 45;
    
    // Smart abbreviation matching (e.g., "us" -> "USD", "United States")
    const smartMatches = {
        'us': ['USD', 'united states', 'america', 'dollar'],
        'eu': ['EUR', 'euro', 'europe'],
        'uk': ['GBP', 'british', 'pound', 'england'],
        'jp': ['JPY', 'japan', 'yen'],
        'cn': ['CNY', 'china', 'yuan'],
        'au': ['AUD', 'australia', 'australian'],
        'ca': ['CAD', 'canada', 'canadian'],
        'sg': ['SGD', 'singapore'],
        'in': ['INR', 'india', 'rupee'],
        'kr': ['KRW', 'korea', 'won'],
        'br': ['BRL', 'brazil', 'real'],
        'ru': ['RUB', 'russia', 'ruble'],
        'ch': ['CHF', 'switzerland', 'franc'],
        'se': ['SEK', 'sweden', 'krona'],
        'no': ['NOK', 'norway', 'krone'],
        'dk': ['DKK', 'denmark', 'krone'],
        'mx': ['MXN', 'mexico', 'peso'],
        'tr': ['TRY', 'turkey', 'lira'],
        'za': ['ZAR', 'south africa', 'rand'],
        'th': ['THB', 'thailand', 'baht'],
        'my': ['MYR', 'malaysia', 'ringgit'],
        'id': ['IDR', 'indonesia', 'rupiah'],
        'ph': ['PHP', 'philippines', 'peso'],
        'vn': ['VND', 'vietnam', 'dong'],
        'hk': ['HKD', 'hong kong'],
        'tw': ['TWD', 'taiwan'],
        'nz': ['NZD', 'new zealand'],
        'il': ['ILS', 'israel', 'shekel'],
        'ae': ['AED', 'uae', 'dirham'],
        'sa': ['SAR', 'saudi', 'riyal']
    };
    
    // Check smart matches
    if (smartMatches[q]) {
        const matches = smartMatches[q];
        if (matches.some(match => code.includes(match) || name.includes(match) || country.includes(match))) {
            score += 75;
        }
    }
    
    // Phonetic and common misspelling corrections
    const phoneticMatches = {
        'dollar': ['usd', 'us', 'america'],
        'euro': ['eur', 'eu', 'europe'],
        'pound': ['gbp', 'uk', 'british'],
        'yen': ['jpy', 'jp', 'japan'],
        'yuan': ['cny', 'cn', 'china'],
        'rupee': ['inr', 'in', 'india'],
        'won': ['krw', 'kr', 'korea'],
        'franc': ['chf', 'ch', 'switzerland'],
        'krona': ['sek', 'se', 'sweden'],
        'peso': ['mxn', 'mx', 'mexico']
    };
    
    Object.entries(phoneticMatches).forEach(([key, values]) => {
        if (q.includes(key) && values.some(v => code.includes(v) || name.includes(v) || country.includes(v))) {
            score += 40;
        }
    });
    
    // Partial word matching for multi-word queries
    const words = q.split(' ');
    words.forEach(word => {
        if (word.length > 2) {
            if (name.includes(word) || country.includes(word)) {
                score += 30;
            }
        }
    });
    
    return score;
}

// Intelligent search function
function searchCurrencies(query) {
    if (!query || query.length < 1) {
        return SUPPORTED_CURRENCIES.slice(0, 10); // Show top 10 by default
    }
    
    const results = SUPPORTED_CURRENCIES
        .map(currency => ({
            ...currency,
            score: calculateSearchScore(currency, query)
        }))
        .filter(item => item.score > 0)
        .sort((a, b) => b.score - a.score)
        .slice(0, 8); // Limit to 8 results for better UX
    
    return results;
}

// ===== ENHANCED NETWORK UTILITIES =====
// Advanced fetch with retry logic and timeout
async function fetchWithRetry(url, options = {}, retries = CONFIG.RETRY_ATTEMPTS) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), CONFIG.REQUEST_TIMEOUT);
    
    const fetchOptions = {
        ...options,
        signal: controller.signal,
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    };

    for (let attempt = 1; attempt <= retries; attempt++) {
        try {
            const startTime = performance.now();
            const response = await fetch(url, fetchOptions);
            const endTime = performance.now();
            const responseTime = Math.round(endTime - startTime);
            
            clearTimeout(timeoutId);
            
            if (CONFIG.FEATURES.PERFORMANCE_MONITORING) {
                performanceMonitor.recordApiCall(responseTime, false);
            }
            
            if (CONFIG.DEBUG_MODE) {
                console.log(`🌐 ${options.method || 'GET'} ${url} - ${response.status} (${responseTime}ms, attempt ${attempt})`);
            }
            
            return response;
        } catch (error) {
            clearTimeout(timeoutId);
            
            if (CONFIG.FEATURES.PERFORMANCE_MONITORING) {
                performanceMonitor.recordError();
            }
            
            if (attempt === retries) {
                if (CONFIG.DEBUG_MODE) {
                    console.error(`❌ Final attempt failed for ${url}:`, error.message);
                }
                throw new Error(`Network request failed after ${retries} attempts: ${error.message}`);
            }
            
            const delay = Math.min(1000 * Math.pow(2, attempt - 1), 5000); // Exponential backoff, max 5s
            if (CONFIG.DEBUG_MODE) {
                console.warn(`⚠️ Attempt ${attempt} failed for ${url}, retrying in ${delay}ms...`);
            }
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }
}

// Legacy token functions for backwards compatibility
async function fetchTokenFromBackend() {
    const token = await tokenManager.getValidToken();
    jwtToken = token; // Update legacy variable
    return token;
}

async function ensureJwtLoaded() {
    const token = await tokenManager.getValidToken();
    jwtToken = token; // Update legacy variable
    return token;
}

// DOM elements
let fromSearch, toSearch, amount, exRateTxt, exchangeIcon, fromSuggestions, toSuggestions;
let currentFromCurrency = 'USD';
let currentToCurrency = 'SGD';

// Make currency variables globally accessible for HTML modal
window.currentFromCurrency = currentFromCurrency;
window.currentToCurrency = currentToCurrency;
let highlightedIndex = -1;
let currentSuggestions = [];

// Parallel fetch for multiple API endpoints
async function fetchMultipleEndpoints(endpoints) {
    try {
        const promises = endpoints.map(async (endpoint) => {
            const response = await fetch(`${CONFIG.API_BASE_URL}${endpoint.url}`);
            if (!response.ok) {
                throw new Error(`${endpoint.name} failed: ${response.status}`);
            }
            const data = await response.json();
            return { name: endpoint.name, data, success: true };
        });
        
        const results = await Promise.allSettled(promises);
        const successful = results
            .filter(result => result.status === 'fulfilled')
            .map(result => result.value);
        
        const failed = results
            .filter(result => result.status === 'rejected')
            .map(result => result.reason.message);
        
        if (CONFIG.DEBUG_MODE) {
            console.log(`✅ Parallel fetch completed: ${successful.length}/${endpoints.length} successful`);
            if (failed.length > 0) {
                console.warn('Failed endpoints:', failed);
            }
        }
        
        return { successful, failed };
    } catch (error) {
        console.error('Parallel fetch error:', error);
        throw error;
    }
}

// Enhanced currency fetching with caching and parallel support
async function fetchSupportedCurrencies() {
    const cacheKey = 'supported_currencies';
    
    // Check cache first
    const cachedCurrencies = cacheManager.get(cacheKey);
    if (cachedCurrencies) {
        if (CONFIG.DEBUG_MODE) {
            console.log('💾 Using cached currencies:', cachedCurrencies.length);
        }
        performanceMonitor.recordApiCall(0, true);
        return cachedCurrencies;
    }
    
    try {
        if (CONFIG.DEBUG_MODE) {
            console.log('🌐 Fetching currencies from:', `${CONFIG.API_BASE_URL}/api/currencies`);
        }
        
        // Parallel fetch currencies and regions for better performance
        const endpoints = [
            { name: 'currencies', url: buildApiUrl('currencies') },
            { name: 'regions', url: buildApiUrl('regions') }
        ];
        
        const { successful } = await fetchMultipleEndpoints(endpoints);
        
        const currenciesResult = successful.find(r => r.name === 'currencies');
        const regionsResult = successful.find(r => r.name === 'regions');
        
        if (currenciesResult) {
            const currencies = currenciesResult.data.currencies.map(curr => ({
                code: curr.code,
                name: curr.name,
                country: getCountryFromCurrency(curr.code)
            }));
            
            // Cache the result
            cacheManager.set(cacheKey, currencies, CONFIG.CACHE_DURATION.CURRENCIES);
            
            if (CONFIG.DEBUG_MODE) {
                console.log(`✅ Fetched ${currencies.length} currencies from enhanced backend`);
            }
            
            // Store regions data for future use
            if (regionsResult) {
                window.CURRENCY_REGIONS = regionsResult.data.regions;
                cacheManager.set('currency_regions', regionsResult.data.regions, CONFIG.CACHE_DURATION.CURRENCIES);
                if (CONFIG.DEBUG_MODE) {
                    console.log(`✅ Fetched ${regionsResult.data.total_regions} regions`);
                }
            }
            
            return currencies;
        }
        
        throw new Error('No currency data received');
        
    } catch (error) {
        console.error('❌ Error fetching currencies from backend:', error);
        console.warn('⚠️ Using fallback currency list (35 currencies)');
        
        // Cache fallback data for shorter duration
        cacheManager.set(cacheKey, SUPPORTED_CURRENCIES, 5 * 60 * 1000); // 5 minutes
        return SUPPORTED_CURRENCIES;
    }
}

// ===== SEARCH UI FUNCTIONS =====
// Display search suggestions
function displaySuggestions(suggestions, container, isFromInput) {
    container.innerHTML = '';
    currentSuggestions = suggestions;
    highlightedIndex = -1;
    
    if (suggestions.length === 0) {
        container.style.display = 'none';
        return;
    }
    
    suggestions.forEach((currency, index) => {
        const item = document.createElement('div');
        item.className = 'suggestion-item';
        item.dataset.index = index;
        item.dataset.currency = currency.code;
        
        const flag = document.createElement('img');
        flag.className = 'suggestion-flag';
        flag.src = `https://flagcdn.com/48x36/${currency.country}.png`;
        flag.alt = `${currency.name} flag`;
        flag.onerror = () => {
            flag.src = 'https://flagcdn.com/48x36/un.png';
        };
        
        const text = document.createElement('div');
        text.className = 'suggestion-text';
        text.innerHTML = `<span class="suggestion-code">${currency.code}</span><span class="suggestion-name">${currency.name}</span>`;
        
        item.appendChild(flag);
        item.appendChild(text);
        
        // Click handler
        item.addEventListener('click', () => {
            selectCurrency(currency, isFromInput);
        });
        
        container.appendChild(item);
    });
    
    container.style.display = 'block';
}

// Select currency from suggestions
function selectCurrency(currency, isFromInput) {
    if (isFromInput) {
        currentFromCurrency = currency.code;
        window.currentFromCurrency = currency.code; // Sync with global
        fromSearch.value = `${currency.code} - ${currency.name}`;
        fromSearch.dataset.currency = currency.code;
        updateFlagImage(document.getElementById('from-flag'), currency.country);
        fromSuggestions.style.display = 'none';
    } else {
        currentToCurrency = currency.code;
        window.currentToCurrency = currency.code; // Sync with global
        toSearch.value = `${currency.code} - ${currency.name}`;
        toSearch.dataset.currency = currency.code;
        updateFlagImage(document.getElementById('to-flag'), currency.country);
        toSuggestions.style.display = 'none';
    }
    
    console.log(`✅ Currency updated: ${isFromInput ? 'FROM' : 'TO'} = ${currency.code}`);
    console.log(`📊 Current pair: ${currentFromCurrency} → ${currentToCurrency}`);
    
    // Update chart currency pair display immediately (lazy-load if needed)
    ensureChartLoaded().then(api => {
        api.updateChartCurrencyPair(currentFromCurrency, currentToCurrency);
    });
    
    // Auto-fetch exchange rate
    getExchangeRate();
}

// Update flag image
function updateFlagImage(imgElement, countryCode) {
    imgElement.src = `https://flagcdn.com/48x36/${countryCode}.png`;
    imgElement.onerror = () => {
        imgElement.src = 'https://flagcdn.com/48x36/un.png';
    };
}

// Handle keyboard navigation
function handleKeyboardNavigation(event, isFromInput) {
    const suggestions = isFromInput ? fromSuggestions : toSuggestions;
    const items = suggestions.querySelectorAll('.suggestion-item');
    
    if (items.length === 0) return;
    
    switch (event.key) {
        case 'ArrowDown':
            event.preventDefault();
            highlightedIndex = Math.min(highlightedIndex + 1, items.length - 1);
            updateHighlight(items);
            break;
            
        case 'ArrowUp':
            event.preventDefault();
            highlightedIndex = Math.max(highlightedIndex - 1, -1);
            updateHighlight(items);
            break;
            
        case 'Enter':
            event.preventDefault();
            if (highlightedIndex >= 0 && highlightedIndex < currentSuggestions.length) {
                selectCurrency(currentSuggestions[highlightedIndex], isFromInput);
            }
            break;
            
        case 'Escape':
            suggestions.style.display = 'none';
            highlightedIndex = -1;
            break;
    }
}

// Update visual highlight
function updateHighlight(items) {
    items.forEach((item, index) => {
        item.classList.toggle('highlighted', index === highlightedIndex);
    });
}

// Initialize search functionality
function initializeSearchInputs() {
    // Set default values
    fromSearch.value = 'USD - US Dollar';
    fromSearch.dataset.currency = 'USD';
    toSearch.value = 'SGD - Singapore Dollar';
    toSearch.dataset.currency = 'SGD';
    
    // Update initial flags
    updateFlagImage(document.getElementById('from-flag'), 'us');
    updateFlagImage(document.getElementById('to-flag'), 'sg');
    
    // From search input events
    fromSearch.addEventListener('input', (e) => {
        const query = e.target.value;
        const suggestions = searchCurrencies(query);
        displaySuggestions(suggestions, fromSuggestions, true);
    });
    
    fromSearch.addEventListener('focus', (e) => {
        const query = e.target.value;
        const suggestions = searchCurrencies(query);
        displaySuggestions(suggestions, fromSuggestions, true);
    });
    
    fromSearch.addEventListener('keydown', (e) => {
        handleKeyboardNavigation(e, true);
    });
    
    // To search input events
    toSearch.addEventListener('input', (e) => {
        const query = e.target.value;
        const suggestions = searchCurrencies(query);
        displaySuggestions(suggestions, toSuggestions, false);
    });
    
    toSearch.addEventListener('focus', (e) => {
        const query = e.target.value;
        const suggestions = searchCurrencies(query);
        displaySuggestions(suggestions, toSuggestions, false);
    });
    
    toSearch.addEventListener('keydown', (e) => {
        handleKeyboardNavigation(e, false);
    });
    
    // Hide suggestions when clicking outside
    document.addEventListener('click', (e) => {
        if (!fromSearch.contains(e.target) && !fromSuggestions.contains(e.target)) {
            fromSuggestions.style.display = 'none';
        }
        if (!toSearch.contains(e.target) && !toSuggestions.contains(e.target)) {
            toSuggestions.style.display = 'none';
        }
    });
}

// Legacy function for backwards compatibility
function updateFlag(select) {
    const code = select.value;
    const imgTag = select.parentElement.querySelector("img");
    const countryCode = getCountryFromCurrency(code);
    imgTag.src = `https://flagcdn.com/48x36/${countryCode}.png`;
    imgTag.onerror = () => {
        imgTag.src = `https://flagcdn.com/48x36/un.png`;
    };
}

// ===== EXCHANGE RATE FUNCTIONS =====
// Format currency display with consistent thousand separators
function formatCurrencyDisplay(amount, fromCurrency, toCurrency, rate) {
    const totalAmount = (amount * rate).toFixed(2);
    
    // Format left side (input amount) with thousand separators
    const formattedAmount = formatNumberDisplay(amount.toFixed(2));
    
    // Format right side (result) with thousand separators - no currency symbol
    const formattedTotal = formatNumberDisplay(totalAmount);
    
    return `${formattedAmount} ${fromCurrency} = ${toCurrency} ${formattedTotal}`;
}

// Enhanced API response handling with validation
function handleExchangeRateResponse(result, amount, fromCurrency, toCurrency) {
    if (!result || typeof result !== 'object') {
        throw new Error('Invalid API response format');
    }
    
    // Check for explicit error field first
    if (result.error) {
        throw new Error(result.error);
    }
    
    // For /api/rates endpoint, check for conversion_rates (success is implied if this exists)
    if (!result.conversion_rates || typeof result.conversion_rates !== 'object') {
        throw new Error('Missing conversion rates in API response');
    }

    const exchangeRate = result.conversion_rates[toCurrency];
    if (!exchangeRate || isNaN(exchangeRate) || exchangeRate <= 0) {
        showErrorState(`❌ Exchange rate not available for ${toCurrency}`, true);
        return false;
    }

    // Validate amount
    const validation = validateAmount(amount);
    if (!validation.isValid) {
        showErrorState(`❌ ${validation.error}`, false);
        return false;
    }

    const displayText = formatCurrencyDisplay(validation.value, fromCurrency, toCurrency, exchangeRate);
    exRateTxt.innerText = displayText;
    
    // Calculate converted amount
    const convertedAmount = validation.value * exchangeRate;
    
    // Add conversion data to chart (lazy-load if needed)
    ensureChartLoaded().then(api => {
        api.addChartConversionData(fromCurrency, toCurrency, exchangeRate, validation.value, convertedAmount);
    });
    
    // Show success animation
    showSuccessState();
    
    // Announce to screen reader
    announceToScreenReader(`Exchange rate calculated: ${displayText}`);
    
    if (CONFIG.DEBUG_MODE) {
        console.log(`✅ Exchange rate fetched: 1 ${fromCurrency} = ${exchangeRate} ${toCurrency}`);
    }
    
    return true;
}

// Enhanced API error handling
async function handleApiError(response, fromCurrency, toCurrency, amountVal) {
    switch (response.status) {
        case 401:
            try {
                // Clear invalid token and retry
                tokenManager.clearToken();
                const newToken = await tokenManager.getValidToken();
                
                const retryResponse = await fetchWithRetry(buildApiUrl(`rates/${fromCurrency}?targets=${toCurrency}&token=${newToken}`), {
                    method: 'GET',
                    headers: { 'Authorization': `Bearer ${newToken}` }
                });
                
                if (!retryResponse.ok) {
                    throw new Error(`Retry failed: ${retryResponse.status}`);
                }
                
                const retryResult = await retryResponse.json();
                return handleExchangeRateResponse(retryResult, amountVal, fromCurrency, toCurrency);
            } catch (err) {
                showErrorState("🔒 Authentication failed. Please refresh the page.", true);
                console.error("JWT token refresh failed:", err);
            }
            break;
            
        case 403:
            showErrorState("🚫 Access denied. Invalid token.", false);
            console.error("Invalid JWT token. Please verify your token.");
            break;
            
        case 429:
            showErrorState("⏳ Rate limit exceeded. Please wait before trying again.", true);
            console.error("Rate limit exceeded. Please wait before making another request.");
            break;
            
        case 404:
            showErrorState(`❌ Currency ${fromCurrency} not found.`, false);
            break;
            
        case 500:
            showErrorState("🔧 Server error. Please try again later.", true);
            break;
            
        default:
            showErrorState(`❌ HTTP ${response.status}: ${response.statusText}`, true);
    }
}

// Legacy function for backwards compatibility
async function handleTokenRefreshAndRetry(fromCurrency, toCurrency, amount) {
    return await handleApiError({ status: 401 }, fromCurrency, toCurrency, amount);
}

// Enhanced batch fetching with caching and retry logic
async function fetchBatchExchangeRates(currencies) {
    const cacheKey = `batch_${currencies.sort().join('_')}`;
    
    // Check cache first
    const cachedBatch = cacheManager.get(cacheKey);
    if (cachedBatch) {
        if (CONFIG.DEBUG_MODE) {
            console.log(`💾 Using cached batch rates for ${currencies.length} currencies`);
        }
        performanceMonitor.recordApiCall(0, true);
        return cachedBatch;
    }
    
    try {
        const JWT_TOKEN = await tokenManager.getValidToken();
        const response = await fetchWithRetry(buildApiUrl(`rates/batch?token=${JWT_TOKEN}`), {
            method: 'POST',
            headers: { 
                'Authorization': `Bearer ${JWT_TOKEN}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ currencies: currencies })
        });

        if (!response.ok) {
            throw new Error(`Batch request failed: ${response.status}`);
        }

        const result = await response.json();
        
        // Cache the successful result
        cacheManager.set(cacheKey, result);
        
        if (CONFIG.DEBUG_MODE) {
            console.log(`✅ Batch exchange rates fetched for ${result.successful_count}/${result.total_requested} currencies in ${result.response_time_ms}ms`);
        }
        
        return result;
    } catch (error) {
        console.error("❌ Error fetching batch exchange rates:", error);
        throw error;
    }
}

// Enhanced exchange rate fetching with caching and parallel support
async function getExchangeRateOptimized(targetCurrencies = null) {
    const amountVal = parseFloat(parseNumberInput(amount.value)) || 1;
    // Use window globals to ensure we get the latest values
    const fromCurrency = window.currentFromCurrency || currentFromCurrency;
    const toCurrency = window.currentToCurrency || currentToCurrency;
    
    // Update local variables to match globals
    currentFromCurrency = fromCurrency;
    currentToCurrency = toCurrency;
    
    if (CONFIG.DEBUG_MODE) {
        console.log(`🔄 Converting: ${amountVal} ${fromCurrency} → ${toCurrency}`);
    }
    
    // Check cache first
    const cacheKey = `rate_${fromCurrency}_${toCurrency}`;
    const cachedRate = cacheManager.get(cacheKey);
    
    if (cachedRate) {
        exRateTxt.innerText = formatCurrencyDisplay(amountVal, fromCurrency, toCurrency, cachedRate.rate);
        if (CONFIG.DEBUG_MODE) {
            console.log(`💾 Using cached rate: 1 ${fromCurrency} = ${cachedRate.rate} ${toCurrency}`);
        }
        performanceMonitor.recordApiCall(0, true);
        return;
    }
    
    // Show loading state with animation
    showLoadingState("Getting exchange rate...");
    
    try {
        // If multiple target currencies are specified, use batch API
        if (targetCurrencies && targetCurrencies.length > 1) {
            const batchResult = await fetchBatchExchangeRates([fromCurrency]);
            if (batchResult.successful_count > 0) {
                const rates = batchResult.results[fromCurrency];
                if (rates && rates.conversion_rates[toCurrency]) {
                    const exchangeRate = rates.conversion_rates[toCurrency];
                    
                    // Cache the result
                    cacheManager.set(cacheKey, { rate: exchangeRate, timestamp: Date.now() });
                    
                    exRateTxt.innerText = formatCurrencyDisplay(amountVal, fromCurrency, toCurrency, exchangeRate);
                    if (CONFIG.DEBUG_MODE) {
                        console.log(`✅ Batch exchange rate: 1 ${fromCurrency} = ${exchangeRate} ${toCurrency}`);
                    }
                    hideLoadingState();
                    return;
                }
            }
        }
        
        // Fallback to single currency API with enhanced error handling
        const JWT_TOKEN = await tokenManager.getValidToken();
        const targets = targetCurrencies || [toCurrency];
        const targetParam = `?targets=${targets.join(',')}`;
        const tokenParam = `${targetParam}&token=${JWT_TOKEN}`;
        
        const response = await fetchWithRetry(buildApiUrl(`rates/${fromCurrency}${tokenParam}`), {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${JWT_TOKEN}` }
        });
        
        if (!response.ok) {
            await handleApiError(response, fromCurrency, toCurrency, amountVal);
            return;
        }

        const result = await response.json();
        const success = handleExchangeRateResponse(result, amountVal, fromCurrency, toCurrency);
        
        if (success && result.conversion_rates && result.conversion_rates[toCurrency]) {
            // Cache the successful result
            cacheManager.set(cacheKey, { 
                rate: result.conversion_rates[toCurrency], 
                timestamp: Date.now() 
            });
        }
        
        // Log performance metrics if available
        if (result.response_time_ms && CONFIG.DEBUG_MODE) {
            console.log(`⚡ API response time: ${result.response_time_ms}ms (${result.cache_hit ? 'cached' : 'fresh'})`);
        }
        
        hideLoadingState();
        
    } catch (error) {
        console.error("❌ Error fetching exchange rate:", error);
        hideLoadingState();
        
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            showErrorState("🌐 Network error. Please check your connection.", true);
        } else if (error.message.includes('timeout') || error.message.includes('aborted')) {
            showErrorState("⏱️ Request timeout. Please try again.", true);
        } else {
            showErrorState(`❌ Error: ${error.message}`, true);
        }
    }
}

// Main exchange rate fetching function (backwards compatible)
async function getExchangeRate() {
    return await getExchangeRateOptimized();
}

// Currency swap functionality
function swapCurrencies() {
    // Swap currency codes
    const tempCurrency = currentFromCurrency;
    currentFromCurrency = currentToCurrency;
    currentToCurrency = tempCurrency;
    
    // Swap display values
    const tempValue = fromSearch.value;
    fromSearch.value = toSearch.value;
    toSearch.value = tempValue;
    
    // Update chart currency pair display immediately
    updateChartCurrencyPair(currentFromCurrency, currentToCurrency);
    
    // Swap data attributes
    const tempData = fromSearch.dataset.currency;
    fromSearch.dataset.currency = toSearch.dataset.currency;
    toSearch.dataset.currency = tempData;
    
    // Swap flag images
    const fromFlag = document.getElementById('from-flag');
    const toFlag = document.getElementById('to-flag');
    const tempSrc = fromFlag.src;
    fromFlag.src = toFlag.src;
    toFlag.src = tempSrc;
    
    // Fetch new rate after swap
    getExchangeRate();
}

// ===== UI ENHANCEMENT FUNCTIONS =====
// Loading state management
function showLoadingState(message = "Loading...") {
    if (exRateTxt) {
        exRateTxt.innerHTML = `
            <div class="loading-container">
                <div class="loading-spinner"></div>
                <span class="loading-text">${message}</span>
            </div>
        `;
        exRateTxt.classList.add('loading');
    }
}

function hideLoadingState() {
    if (exRateTxt) {
        exRateTxt.classList.remove('loading');
    }
}

// Error state management
function showErrorState(message, canRetry = false) {
    if (exRateTxt) {
        const retryButton = canRetry ? 
            `<button class="retry-btn" onclick="getExchangeRate()" aria-label="Retry exchange rate fetch">
                <i class="fas fa-redo" aria-hidden="true"></i> Retry
            </button>` : '';
        
        exRateTxt.innerHTML = `
            <div class="error-container">
                <span class="error-text">${message}</span>
                ${retryButton}
            </div>
        `;
        exRateTxt.classList.add('error');
        
        // Auto-remove error class after animation
        setTimeout(() => {
            if (exRateTxt) {
                exRateTxt.classList.remove('error');
            }
        }, 300);
    }
}

// Success state with animation
function showSuccessState(message) {
    if (exRateTxt) {
        exRateTxt.classList.add('success');
        setTimeout(() => {
            if (exRateTxt) {
                exRateTxt.classList.remove('success');
            }
        }, 1000);
    }
}

// ===== ACCESSIBILITY ENHANCEMENTS =====
// Keyboard shortcuts
function initializeKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Only trigger shortcuts when not typing in inputs
        if (e.target.tagName === 'INPUT' && e.target.type === 'text') {
            return;
        }
        
        switch (e.key) {
            case 'Enter':
                if (e.ctrlKey || e.metaKey) {
                    e.preventDefault();
                    getExchangeRate();
                    announceToScreenReader('Exchange rate calculation started');
                }
                break;
                
            case 's':
                if (e.ctrlKey || e.metaKey) {
                    e.preventDefault();
                    swapCurrencies();
                    announceToScreenReader('Currencies swapped');
                }
                break;
                
            case 'r':
                if (e.ctrlKey || e.metaKey) {
                    e.preventDefault();
                    resetForm();
                    announceToScreenReader('Form reset to default values');
                }
                break;
                
            case 'Escape':
                // Close any open suggestions
                if (fromSuggestions) fromSuggestions.style.display = 'none';
                if (toSuggestions) toSuggestions.style.display = 'none';
                break;
        }
    });
}

// Screen reader announcements
function announceToScreenReader(message) {
    if (!CONFIG.FEATURES.ACCESSIBILITY) return;
    
    const announcement = document.createElement('div');
    announcement.setAttribute('aria-live', 'polite');
    announcement.setAttribute('aria-atomic', 'true');
    announcement.className = 'sr-only';
    announcement.textContent = message;
    
    document.body.appendChild(announcement);
    
    // Remove after announcement
    setTimeout(() => {
        document.body.removeChild(announcement);
    }, 1000);
}

// Enhanced reset function with app refresh
function resetForm() {
    // Clear all caches first
    cacheManager.clear();
    tokenManager.clearToken();
    
    // Reset performance metrics
    performanceMonitor.reset();
    
    if (fromSearch && toSearch && amount) {
        fromSearch.value = 'USD - US Dollar';
        fromSearch.dataset.currency = 'USD';
        toSearch.value = 'SGD - Singapore Dollar';
        toSearch.dataset.currency = 'SGD';
        currentFromCurrency = 'USD';
        currentToCurrency = 'SGD';
        amount.value = '';
        
        updateFlagImage(document.getElementById('from-flag'), 'us');
        updateFlagImage(document.getElementById('to-flag'), 'sg');
        
        if (exRateTxt) {
            exRateTxt.innerText = "Ready to convert";
            exRateTxt.classList.remove('loading', 'error', 'success');
        }
        
        // Clear chart data and reset currency pair display regardless of lazy load
        const chartFromDisplay = document.querySelector('.chart-from');
        const chartToDisplay = document.querySelector('.chart-to');
        const chartCurrentRate = document.querySelector('.chart-current-rate');

        if (chartFromDisplay) chartFromDisplay.textContent = '--';
        if (chartToDisplay) chartToDisplay.textContent = '--';
        if (chartCurrentRate) chartCurrentRate.textContent = '';

        // Clear chart data if chart exists and reset pair to defaults
        if (window.exchangeChart) {
            window.exchangeChart.clearChartData();
            // Reset base rate and current pair
            window.exchangeChart.baseRate = undefined;
            window.exchangeChart.updateCurrencyPair('USD', 'SGD');
        } else {
            // If chart not yet loaded, set default indicator now
            if (chartFromDisplay) chartFromDisplay.textContent = '1 USD';
            if (chartToDisplay) chartToDisplay.textContent = 'SGD';
        }
        
        if (fromSuggestions) fromSuggestions.style.display = 'none';
        if (toSuggestions) toSuggestions.style.display = 'none';
        
        // Reload currencies and refresh app state
        setTimeout(() => {
            // Refresh currencies from backend
            if (typeof fetchSupportedCurrencies === 'function') {
                fetchSupportedCurrencies();
            }
            
            // Show visible notification
            if (window.KconvertModal) {
                window.KconvertModal.open({
                    title: 'Reset Complete',
                    message: 'Application has been reset to default settings with fresh data from server.',
                    icon: 'check'
                });
            }
            
            announceToScreenReader('Application refreshed with default settings');
        }, 100);
    }
    
    console.log('🔄 App reset: Cleared caches, reset form, refreshed data');
}

// ===== PERFORMANCE MONITORING DASHBOARD =====
// Debug performance dashboard (only in development)
function initializePerformanceDashboard() {
    if (!CONFIG.DEBUG_MODE || !CONFIG.FEATURES.PERFORMANCE_MONITORING) return;
    
    // Add performance stats to console every 30 seconds
    setInterval(() => {
        const stats = performanceMonitor.getStats();
        const cacheStats = cacheManager.getStats();
        
        console.group('📊 Performance Dashboard');
        console.log('API Calls:', stats.apiCalls);
        console.log('Cache Hit Rate:', stats.cacheHitRate);
        console.log('Average Response Time:', Math.round(stats.averageResponseTime) + 'ms');
        console.log('Errors:', stats.errors);
        console.log('Cache Size:', cacheStats.size, 'items');
        console.log('Cached Keys:', cacheStats.keys);
        console.groupEnd();
    }, 30000);
    
    // Add keyboard shortcut to show stats
    document.addEventListener('keydown', (e) => {
        if (e.key === 'F12' && e.shiftKey) {
            e.preventDefault();
            const stats = performanceMonitor.getStats();
            const cacheStats = cacheManager.getStats();
            
            alert(`Performance Stats:\n` +
                  `API Calls: ${stats.apiCalls}\n` +
                  `Cache Hit Rate: ${stats.cacheHitRate}\n` +
                  `Avg Response Time: ${Math.round(stats.averageResponseTime)}ms\n` +
                  `Errors: ${stats.errors}\n` +
                  `Cache Size: ${cacheStats.size} items`);
        }
    });
}

// ===== OFFLINE MODE SUPPORT =====
// Detect online/offline status
function initializeOfflineMode() {
    if (!CONFIG.FEATURES.OFFLINE_MODE) return;
    
    function updateOnlineStatus() {
        const isOnline = navigator.onLine;
        const statusIndicator = document.querySelector('.connection-status');
        
        if (statusIndicator) {
            statusIndicator.textContent = isOnline ? '🟢 Online' : '🔴 Offline';
            statusIndicator.className = `connection-status ${isOnline ? 'online' : 'offline'}`;
        }
        
        if (!isOnline && exRateTxt) {
            showErrorState('🌐 You are offline. Some features may not work.', false);
        }
        
        if (CONFIG.DEBUG_MODE) {
            console.log(`📡 Connection status: ${isOnline ? 'Online' : 'Offline'}`);
        }
    }
    
    window.addEventListener('online', updateOnlineStatus);
    window.addEventListener('offline', updateOnlineStatus);
    
    // Initial status check
    updateOnlineStatus();
}

// Initialize the application with parallel loading
async function initApp() {
    if (CONFIG.DEBUG_MODE) {
        console.log('🚀 Initializing Enhanced Currency Converter...');
    }
    
    // Get DOM elements for new searchable interface
    fromSearch = document.querySelector(".from-search");
    toSearch = document.querySelector(".to-search");
    fromSuggestions = document.querySelector(".from-suggestions");
    toSuggestions = document.querySelector(".to-suggestions");
    amount = document.querySelector("#amount-input");
    exRateTxt = document.querySelector(".result");
    
    // Initialize number formatting for amount input
    initializeNumberFormatting();
    exchangeIcon = document.querySelector(".reverse");

    // Parallel initialization tasks
    const initTasks = [
        // Task 1: Load currency data from backend
        fetchSupportedCurrencies().then(currencies => {
            if (currencies && currencies.length > 35) {
                // Update global currency list with backend data
                SUPPORTED_CURRENCIES.length = 0;
                SUPPORTED_CURRENCIES.push(...currencies);
                if (CONFIG.DEBUG_MODE) {
                    console.log(`✅ Updated currency list: ${currencies.length} currencies loaded`);
                }
            }
            return currencies;
        }),
        
        // Task 2: Pre-fetch JWT token
        ensureJwtLoaded().then(token => {
            if (CONFIG.DEBUG_MODE) {
                console.log('✅ JWT token pre-loaded');
            }
            return token;
        }).catch(err => {
            if (CONFIG.DEBUG_MODE) {
                console.warn('⚠️ JWT pre-load failed, will fetch on demand:', err.message);
            }
            return null;
        }),
        
        // Task 3: Initialize UI components
        Promise.resolve().then(() => {
            initializeSearchInputs();
            // Auto-load chart when chart container becomes visible
            const chartContainer = document.querySelector('.chart-container');
            if (chartContainer) {
                const observer = new IntersectionObserver(async (entries, obs) => {
                    const entry = entries[0];
                    if (entry && entry.isIntersecting) {
                        const api = await ensureChartLoaded();
                        api.updateChartCurrencyPair(currentFromCurrency, currentToCurrency);
                        obs.disconnect();
                    }
                }, { root: null, rootMargin: '120px', threshold: 0.01 });
                observer.observe(chartContainer);
            }
            if (CONFIG.DEBUG_MODE) {
                console.log('✅ Search UI and Chart initialized');
            }
            return true;
        })
    ];
    
    // Execute all initialization tasks in parallel
    try {
        const [currencyData, jwtToken, uiReady] = await Promise.allSettled(initTasks);
        
        if (CONFIG.DEBUG_MODE) {
            console.log('📊 Parallel initialization results:');
            console.log(`   - Currency data: ${currencyData.status === 'fulfilled' ? '✅' : '❌'}`);
            console.log(`   - JWT token: ${jwtToken.status === 'fulfilled' ? '✅' : '⚠️'}`);
            console.log(`   - UI components: ${uiReady.status === 'fulfilled' ? '✅' : '❌'}`);
        }
        
    } catch (error) {
        console.error('❌ Initialization error:', error);
    }

    // Add event listeners
    exchangeIcon.addEventListener("click", swapCurrencies);
    
    // Add convert button event listener
    const convertButton = document.querySelector("#convert-button");
    if (convertButton) {
        convertButton.addEventListener("click", (e) => {
            e.preventDefault();
            getExchangeRate();
        });
    }

    // Add reset button event listener
    const resetButton = document.querySelector("#reset-button");
    if (resetButton) {
        resetButton.addEventListener("click", (e) => {
            e.preventDefault();
            resetForm();
            announceToScreenReader('Form has been reset to default values');
        });
    }
    
    // Initialize enhanced features
    initializeKeyboardShortcuts();
    initializePerformanceDashboard();
    initializeOfflineMode();

    // Initial display message is hidden; use the Help icon to show usage modal instead
    if (exRateTxt) exRateTxt.innerText = "";

    // Wire the Help icon to open the floating modal with the same instruction copy
    const helpIcon = document.getElementById('help-icon');
    const openHelp = () => {
        const helpMessage = "Enter amount and click 'Convert' (or press Enter). Click 'Reset' to start over.";
        if (window.KconvertModal && typeof window.KconvertModal.open === 'function') {
            window.KconvertModal.open({ title: 'How to use', message: helpMessage });
        } else {
            alert(helpMessage);
        }
    };
    if (helpIcon) {
        helpIcon.addEventListener('click', openHelp);
        helpIcon.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                openHelp();
            }
        });
    }
    
    if (CONFIG.DEBUG_MODE) {
        console.log('🚀 Enhanced Currency Converter initialized successfully!');
        console.log(`🔧 Environment: ${CONFIG.IS_PRODUCTION ? 'Production (pro)' : 'Development (dev)'}`);
        console.log('📈 Performance features enabled:');
        console.log('   - Intelligent caching system (5min rates, 24h currencies)');
        console.log('   - Parallel API loading with retry logic');
        console.log('   - Batch exchange rate fetching');
        console.log('   - Performance monitoring dashboard');
        console.log('   - Exponential backoff retry strategy');
        console.log('🔍 Smart search features enabled:');
        console.log('   - Fuzzy matching with ML-like scoring');
        console.log('   - Multi-language search support');
        console.log('   - Phonetic matching and typo correction');
        console.log('   - Keyboard navigation (↑↓ arrows, Enter, Escape)');
        console.log('♿ Accessibility features enabled:');
        console.log('   - Screen reader announcements');
        console.log('   - Keyboard shortcuts (Ctrl+Enter, Ctrl+S, Ctrl+R)');
        console.log('   - ARIA labels and live regions');
        console.log('   - Focus management');
        console.log('🌐 Network features enabled:');
        console.log('   - Offline mode detection');
        console.log('   - Connection status indicator');
        console.log('   - Smart error recovery');
        console.log('   - Request timeout and abort handling');
        console.log('💡 Press Shift+F12 for performance stats');
    } else if (CONFIG.IS_PRODUCTION) {
        console.log('🚀 Currency Converter - Production Mode');
    }
}

// ===== NUMBER FORMATTING FUNCTIONS =====
// Enhanced number formatting with accessibility
function initializeNumberFormatting() {
    if (!amount) return;
    
    // Add ARIA attributes for accessibility
    amount.setAttribute('aria-label', 'Enter amount to convert');
    amount.setAttribute('aria-describedby', 'amount-help');
    amount.setAttribute('inputmode', 'decimal');
    
    // Create help text for screen readers
    const helpText = document.createElement('div');
    helpText.id = 'amount-help';
    helpText.className = 'sr-only';
    helpText.textContent = 'Enter a numeric amount. Use decimal point for cents.';
    amount.parentNode.insertBefore(helpText, amount.nextSibling);
    
    let lastValidValue = '';
    
    amount.addEventListener('input', (e) => {
        let value = e.target.value;
        
        // Remove all characters except digits and dots
        value = value.replace(/[^0-9.]/g, "");
        
        // Prevent multiple decimal points
        const dotCount = (value.match(/\./g) || []).length;
        if (dotCount > 1) {
            // Keep only the first dot
            const firstDotIndex = value.indexOf('.');
            value = value.substring(0, firstDotIndex + 1) + value.substring(firstDotIndex + 1).replace(/\./g, '');
        }
        
        // Limit decimal places to 2
        if (value.includes('.')) {
            const [integerPart, decimalPart] = value.split('.');
            if (decimalPart && decimalPart.length > 2) {
                value = `${integerPart}.${decimalPart.substring(0, 2)}`;
            }
        }
        
        // Split into integer and decimal parts
        let [integerPart, decimalPart] = value.split(".");
        
        // Format integer part with thousand separators
        if (integerPart && integerPart.length > 0) {
            // Remove leading zeros except for single zero
            integerPart = integerPart.replace(/^0+/, '') || '0';
            
            // Prevent numbers that are too large
            if (integerPart.replace(/,/g, '').length > 12) {
                e.target.value = lastValidValue;
                announceToScreenReader('Maximum amount exceeded');
                return;
            }
            
            // Add thousand separators using Intl.NumberFormat
            if (integerPart !== '0' || value === '0') {
                try {
                    integerPart = new Intl.NumberFormat("en-US").format(parseInt(integerPart, 10));
                } catch (error) {
                    console.warn('Number formatting error:', error);
                    integerPart = integerPart.replace(/,/g, ''); // Remove commas as fallback
                }
            }
        }
        
        // Reconstruct the value
        const formattedValue = decimalPart !== undefined ? `${integerPart}.${decimalPart}` : integerPart;
        e.target.value = formattedValue;
        lastValidValue = formattedValue;
    });
    
    // Handle paste events with validation
    amount.addEventListener('paste', (e) => {
        setTimeout(() => {
            // Trigger input event after paste
            amount.dispatchEvent(new Event('input'));
            
            // Announce to screen reader
            const value = parseNumberInput(amount.value);
            if (value && !isNaN(value)) {
                announceToScreenReader(`Amount set to ${value}`);
            }
        }, 0);
    });
    
    // Auto-convert on Enter key
    amount.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            getExchangeRate();
        }
    });
    
    // Announce value changes to screen reader
    amount.addEventListener('blur', () => {
        const value = parseNumberInput(amount.value);
        if (value && !isNaN(value) && value > 0) {
            announceToScreenReader(`Amount: ${formatNumberDisplay(value)}`);
        }
    });
}

// Enhanced number parsing with validation
function parseNumberInput(formattedValue) {
    if (!formattedValue && formattedValue !== 0) return '';
    
    // Convert to string if it's not already
    const stringValue = String(formattedValue);
    
    // Remove thousand separators (commas) but keep decimal point
    const cleaned = stringValue.replace(/,/g, '');
    
    // Validate the result
    if (isNaN(cleaned) || cleaned === '') return '';
    
    return cleaned;
}

// Enhanced number formatting with locale support
function formatNumberDisplay(number) {
    if (!number && number !== 0) return '';
    
    try {
        const numStr = String(number);
        const [integerPart, decimalPart] = numStr.split('.');
        
        const formattedInteger = new Intl.NumberFormat("en-US").format(parseInt(integerPart, 10));
        
        return decimalPart !== undefined ? `${formattedInteger}.${decimalPart}` : formattedInteger;
    } catch (error) {
        console.warn('Number formatting error:', error);
        return String(number); // Fallback to string conversion
    }
}

// Validate currency amount
function validateAmount(value) {
    const num = parseFloat(parseNumberInput(value));
    return {
        isValid: !isNaN(num) && num > 0 && num <= 999999999999,
        value: num,
        error: isNaN(num) ? 'Invalid number' : 
               num <= 0 ? 'Amount must be greater than zero' :
               num > 999999999999 ? 'Amount too large' : null
    };
}

// ===== APPLICATION STARTUP =====
// Enhanced startup with error handling
function startApp() {
    try {
        initApp();
    } catch (error) {
        console.error('❌ Failed to initialize app:', error);
        
        // Fallback initialization
        setTimeout(() => {
            try {
                console.log('🔄 Attempting fallback initialization...');
                initApp();
            } catch (fallbackError) {
                console.error('❌ Fallback initialization failed:', fallbackError);
                
                // Show user-friendly error
                const errorDiv = document.createElement('div');
                errorDiv.innerHTML = `
                    <div style="padding: 20px; background: #fee; border: 1px solid #fcc; border-radius: 8px; margin: 20px; text-align: center;">
                        <h3>⚠️ Application Error</h3>
                        <p>The currency converter failed to initialize properly.</p>
                        <button onclick="location.reload()" style="padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">
                            🔄 Reload Page
                        </button>
                    </div>
                `;
                document.body.insertBefore(errorDiv, document.body.firstChild);
            }
        }, 1000);
    }
}

// Start the application when DOM is loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', startApp);
} else {
    // DOM is already loaded
    startApp();
}

// ===== GLOBAL ERROR HANDLING =====
// Catch unhandled errors
window.addEventListener('error', (event) => {
    console.error('🚨 Unhandled error:', event.error);
    
    if (CONFIG.FEATURES.ERROR_REPORTING) {
        // Could send to error reporting service here
        performanceMonitor.recordError();
    }
});

// Catch unhandled promise rejections
window.addEventListener('unhandledrejection', (event) => {
    console.error('🚨 Unhandled promise rejection:', event.reason);
    
    if (CONFIG.FEATURES.ERROR_REPORTING) {
        performanceMonitor.recordError();
    }
    
    // Prevent the default browser behavior
    event.preventDefault();
});

// ===== EXPORT FOR TESTING =====
// Export functions for testing (only in development mode)
if (CONFIG.DEBUG_MODE && typeof window !== 'undefined') {
    window.CurrencyConverter = {
        CONFIG,
        tokenManager,
        cacheManager,
        performanceMonitor,
        searchCurrencies,
        selectCurrency,
        getExchangeRate,
        swapCurrencies,
        resetForm,
        fetchSupportedCurrencies,
        fetchBatchExchangeRates
    };
    console.log('🧪 Testing exports available in window.CurrencyConverter');
}
