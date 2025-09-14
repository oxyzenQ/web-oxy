// Currency Converter - Consolidated Main JS File
// All functionality in one file for easy maintenance

// Import fonts and icons
import '@fontsource/inter/400.css';
import '@fontsource/inter/500.css';
import '@fontsource/inter/600.css';
import '@fortawesome/fontawesome-free/css/all.css';

// Configuration (Vite-native env)
const CONFIG = {
    API_BASE_URL: import.meta?.env?.VITE_API_BASE_URL || 
                 (location.protocol === 'https:' ? 'https://kconvert-backend.zeabur.app' : 'http://localhost:8000'),
    REQUEST_TIMEOUT: 10000,
    RETRY_ATTEMPTS: 3,
    CURRENCY_UPDATE_INTERVAL: 60000,
    DEBUG_MODE: import.meta?.env?.MODE === 'development',
    FEATURES: {
        RATE_LIMITING_UI: true,
        ERROR_REPORTING: true,
        DEBUG_LOGGING: import.meta?.env?.MODE === 'development'
    }
};
window.APP_CONFIG = CONFIG;

// JWT token management
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

async function fetchTokenFromBackend() {
    const res = await fetch(`${CONFIG.API_BASE_URL}/api/auth`, { cache: 'no-store' });
    if (!res.ok) throw new Error(`Auth failed: ${res.status}`);
    const data = await res.json();
    if (!data?.token) throw new Error('No token in auth response');
    jwtToken = String(data.token);
    return jwtToken;
}

async function ensureJwtLoaded() {
    if (jwtToken) return jwtToken;
    return await fetchTokenFromBackend();
}

// DOM elements
let fromSearch, toSearch, amount, exRateTxt, exchangeIcon, fromSuggestions, toSuggestions;
let currentFromCurrency = 'USD';
let currentToCurrency = 'SGD';
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

// Enhanced currency fetching with parallel support
async function fetchSupportedCurrencies() {
    try {
        if (CONFIG.DEBUG_MODE) {
            console.log('Fetching currencies from:', `${CONFIG.API_BASE_URL}/api/currencies`);
        }
        
        // Parallel fetch currencies and regions for better performance
        const endpoints = [
            { name: 'currencies', url: '/api/currencies' },
            { name: 'regions', url: '/api/regions' }
        ];
        
        const { successful } = await fetchMultipleEndpoints(endpoints);
        
        const currenciesResult = successful.find(r => r.name === 'currencies');
        const regionsResult = successful.find(r => r.name === 'regions');
        
        if (currenciesResult) {
            if (CONFIG.DEBUG_MODE) {
                console.log(`Fetched ${currenciesResult.data.currencies.length} currencies from enhanced backend`);
            }
            
            // Store regions data for future use
            if (regionsResult) {
                window.CURRENCY_REGIONS = regionsResult.data.regions;
                if (CONFIG.DEBUG_MODE) {
                    console.log(`Fetched ${regionsResult.data.total_regions} regions`);
                }
            }
            
            return currenciesResult.data.currencies.map(curr => ({
                code: curr.code,
                name: curr.name,
                country: getCountryFromCurrency(curr.code)
            }));
        }
        
        throw new Error('No currency data received');
        
    } catch (error) {
        console.error('Error fetching currencies from backend:', error);
        console.warn('Using fallback currency list (35 currencies)');
        // Fallback to centralized currency data
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
        fromSearch.value = `${currency.code} - ${currency.name}`;
        fromSearch.dataset.currency = currency.code;
        updateFlagImage(document.getElementById('from-flag'), currency.country);
        fromSuggestions.style.display = 'none';
    } else {
        currentToCurrency = currency.code;
        toSearch.value = `${currency.code} - ${currency.name}`;
        toSearch.dataset.currency = currency.code;
        updateFlagImage(document.getElementById('to-flag'), currency.country);
        toSuggestions.style.display = 'none';
    }
    
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

// Handle API response and display result
function handleExchangeRateResponse(result, amount, fromCurrency, toCurrency) {
    if (!result.success) {
        throw new Error('API returned unsuccessful response');
    }

    const exchangeRate = result.conversion_rates[toCurrency];
    if (!exchangeRate) {
        exRateTxt.innerText = `❌ Exchange rate not available for ${toCurrency}`;
        return false;
    }

    exRateTxt.innerText = formatCurrencyDisplay(amount, fromCurrency, toCurrency, exchangeRate);
    if (CONFIG.DEBUG_MODE) {
        console.log(`✅ Exchange rate fetched: 1 ${fromCurrency} = ${exchangeRate} ${toCurrency}`);
    }
    return true;
}

// Handle token refresh and retry
async function handleTokenRefreshAndRetry(fromCurrency, toCurrency, amount) {
    try {
        await fetchTokenFromBackend();
        const retry = await fetch(`${CONFIG.API_BASE_URL}/api/rates/${fromCurrency}?token=${jwtToken}`, {
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
        if (!retry.ok) throw new Error(`HTTP ${retry.status}`);
        const retryResult = await retry.json();
        return handleExchangeRateResponse(retryResult, amount, fromCurrency, toCurrency);
    } catch (err) {
        exRateTxt.innerText = "🔒 Token expired. Please try again.";
        console.error("JWT token refresh failed.", err);
        return false;
    }
}

// Parallel batch fetching for multiple currencies
async function fetchBatchExchangeRates(currencies) {
    try {
        const JWT_TOKEN = await ensureJwtLoaded();
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/rates/batch?token=${JWT_TOKEN}`, {
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
        if (CONFIG.DEBUG_MODE) {
            console.log(`✅ Batch exchange rates fetched for ${result.successful_count}/${result.total_requested} currencies in ${result.response_time_ms}ms`);
        }
        return result;
    } catch (error) {
        console.error("Error fetching batch exchange rates:", error);
        throw error;
    }
}

// Enhanced exchange rate fetching with parallel support
async function getExchangeRateOptimized(targetCurrencies = null) {
    const amountVal = parseFloat(parseNumberInput(amount.value)) || 1;
    const fromCurrency = currentFromCurrency;
    const toCurrency = currentToCurrency;
    
    exRateTxt.innerText = "Getting exchange rate...";
    
    try {
        // If multiple target currencies are specified, use batch API
        if (targetCurrencies && targetCurrencies.length > 1) {
            const batchResult = await fetchBatchExchangeRates([fromCurrency]);
            if (batchResult.successful_count > 0) {
                const rates = batchResult.results[fromCurrency];
                if (rates && rates.conversion_rates[toCurrency]) {
                    const exchangeRate = rates.conversion_rates[toCurrency];
                    exRateTxt.innerText = formatCurrencyDisplay(amountVal, fromCurrency, toCurrency, exchangeRate);
                    if (CONFIG.DEBUG_MODE) {
                        console.log(`✅ Batch exchange rate: 1 ${fromCurrency} = ${exchangeRate} ${toCurrency}`);
                    }
                    return;
                }
            }
        }
        
        // Fallback to single currency API with parallel target filtering
        const JWT_TOKEN = await ensureJwtLoaded();
        const targetParam = targetCurrencies ? `?targets=${targetCurrencies.join(',')}` : '';
        const tokenParam = targetParam ? `${targetParam}&token=${JWT_TOKEN}` : `?token=${JWT_TOKEN}`;
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/rates/${fromCurrency}${tokenParam}`, {
            headers: { 'Authorization': `Bearer ${JWT_TOKEN}` }
        });
        
        if (!response.ok) {
            if (response.status === 401) {
                return await handleTokenRefreshAndRetry(fromCurrency, toCurrency, amountVal);
            } else if (response.status === 403) {
                exRateTxt.innerText = "🚫 Invalid token. Please check your token.";
                console.error("Invalid JWT token. Please verify your token.");
                return;
            } else if (response.status === 429) {
                exRateTxt.innerText = "⏳ Rate limit exceeded. Please try again later.";
                console.error("Rate limit exceeded. Please wait before making another request.");
                return;
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        }

        const result = await response.json();
        handleExchangeRateResponse(result, amountVal, fromCurrency, toCurrency);
        
        // Log performance metrics if available
        if (result.response_time_ms && CONFIG.DEBUG_MODE) {
            console.log(`⚡ API response time: ${result.response_time_ms}ms (${result.cache_hit ? 'cached' : 'fresh'})`);
        }
        
    } catch (error) {
        console.error("Error fetching exchange rate:", error);
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            exRateTxt.innerText = "🌐 Network error. Please check your connection and backend server.";
        } else {
            exRateTxt.innerText = `❌ Error: ${error.message}`;
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
            if (CONFIG.DEBUG_MODE) {
                console.log('✅ Search UI initialized');
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
            // Reset to default values
            fromSearch.value = 'USD - US Dollar';
            fromSearch.dataset.currency = 'USD';
            toSearch.value = 'SGD - Singapore Dollar';
            toSearch.dataset.currency = 'SGD';
            currentFromCurrency = 'USD';
            currentToCurrency = 'SGD';
            amount.value = '';
            updateFlagImage(document.getElementById('from-flag'), 'us');
            updateFlagImage(document.getElementById('to-flag'), 'sg');
            exRateTxt.innerText = "Enter amount and click 'Get Exchange Rate' to convert";
            fromSuggestions.style.display = 'none';
            toSuggestions.style.display = 'none';
        });
    }

    // Initial display message
    exRateTxt.innerText = "Enter amount and click 'Get Exchange Rate' to convert";
    
    if (CONFIG.DEBUG_MODE) {
        console.log('✅ Enhanced Currency Converter initialized successfully!');
        console.log('🚀 Performance features enabled:');
        console.log('   - Parallel API loading (currencies + regions + JWT)');
        console.log('   - Batch exchange rate fetching');
        console.log('   - Optimized target currency filtering');
        console.log('   - Enhanced caching and performance monitoring');
        console.log('🔍 Smart search features enabled:');
        console.log('   - Fuzzy matching (e.g., "us" → USD)');
        console.log('   - Country name search (e.g., "america" → USD)');
        console.log('   - Currency name search (e.g., "dollar" → USD)');
        console.log('   - Keyboard navigation (↑↓ arrows, Enter, Escape)');
        console.log('   - Auto-complete suggestions');
    }
}

// ===== NUMBER FORMATTING FUNCTIONS =====
// Initialize number formatting for amount input
function initializeNumberFormatting() {
    if (!amount) return;
    
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
        
        // Split into integer and decimal parts
        let [integerPart, decimalPart] = value.split(".");
        
        // Format integer part with thousand separators
        if (integerPart && integerPart.length > 0) {
            // Remove leading zeros except for single zero
            integerPart = integerPart.replace(/^0+/, '') || '0';
            
            // Add thousand separators using Intl.NumberFormat
            if (integerPart !== '0' || value === '0') {
                integerPart = new Intl.NumberFormat("en-US").format(parseInt(integerPart, 10));
            }
        }
        
        // Reconstruct the value
        e.target.value = decimalPart !== undefined ? `${integerPart}.${decimalPart}` : integerPart;
    });
    
    // Handle paste events
    amount.addEventListener('paste', (e) => {
        setTimeout(() => {
            // Trigger input event after paste
            amount.dispatchEvent(new Event('input'));
        }, 0);
    });
}

// Parse formatted number input (remove commas) for calculations
function parseNumberInput(formattedValue) {
    if (!formattedValue) return '';
    // Remove thousand separators (commas) but keep decimal point
    return formattedValue.replace(/,/g, '');
}

// Format number for display with thousand separators
function formatNumberDisplay(number) {
    if (!number && number !== 0) return '';
    
    const numStr = String(number);
    const [integerPart, decimalPart] = numStr.split('.');
    
    const formattedInteger = new Intl.NumberFormat("en-US").format(parseInt(integerPart, 10));
    
    return decimalPart !== undefined ? `${formattedInteger}.${decimalPart}` : formattedInteger;
}

// Start the application when DOM is loaded
document.addEventListener('DOMContentLoaded', initApp);
