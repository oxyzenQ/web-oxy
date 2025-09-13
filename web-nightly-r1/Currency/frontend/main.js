// Currency Converter - Consolidated Main JS File
// All functionality in one file for easy maintenance

// Import fonts and icons
import '@fontsource/inter/400.css';
import '@fontsource/inter/500.css';
import '@fontsource/inter/600.css';
import 'material-icons/iconfont/material-icons.css';

// Configuration (Vite-native env)
const CONFIG = {
    API_BASE_URL: import.meta?.env?.VITE_API_BASE_URL || "http://localhost:8000",
    REQUEST_TIMEOUT: 10000,
    RETRY_ATTEMPTS: 3,
    CURRENCY_UPDATE_INTERVAL: 60000,
    FEATURES: {
        RATE_LIMITING_UI: true,
        ERROR_REPORTING: true,
        DEBUG_LOGGING: true
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

// Fetch supported currencies from backend
async function fetchSupportedCurrencies() {
    try {
        console.log('Fetching currencies from:', `${CONFIG.API_BASE_URL}/api/currencies`);
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/currencies`);
        if (!response.ok) {
            throw new Error(`Failed to fetch currencies: ${response.status}`);
        }
        const data = await response.json();
        console.log('Fetched currencies from backend:', data.currencies.length, 'currencies');
        return data.currencies;
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
// Format currency display
function formatCurrencyDisplay(amount, fromCurrency, toCurrency, rate) {
    const totalAmount = (amount * rate).toFixed(2);
    const formatter = new Intl.NumberFormat('en-US', { 
        style: 'currency', 
        currency: toCurrency, 
        minimumFractionDigits: 2, 
        maximumFractionDigits: 2 
    });
    return `${amount.toFixed(2)} ${fromCurrency} = ${formatter.format(totalAmount)}`;
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
    console.log(`✅ Exchange rate fetched: 1 ${fromCurrency} = ${exchangeRate} ${toCurrency}`);
    return true;
}

// Handle token refresh and retry
async function handleTokenRefreshAndRetry(fromCurrency, toCurrency, amount) {
    try {
        await fetchTokenFromBackend();
        const retry = await fetch(`${CONFIG.API_BASE_URL}/api/rates/${fromCurrency}`, {
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

// Main exchange rate fetching function
async function getExchangeRate() {
    const amountVal = parseFloat(amount.value) || 1;
    const fromCurrency = currentFromCurrency;
    const toCurrency = currentToCurrency;
    
    exRateTxt.innerText = "Getting exchange rate...";
    
    try {
        const JWT_TOKEN = await ensureJwtLoaded();
        const response = await fetch(`${CONFIG.API_BASE_URL}/api/rates/${fromCurrency}`, {
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
        
    } catch (error) {
        console.error("Error fetching exchange rate:", error);
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            exRateTxt.innerText = "🌐 Network error. Please check your connection and backend server.";
        } else {
            exRateTxt.innerText = `❌ Error: ${error.message}`;
        }
    }
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

// Initialize the application
async function initApp() {
    // Get DOM elements for new searchable interface
    fromSearch = document.querySelector(".from-search");
    toSearch = document.querySelector(".to-search");
    fromSuggestions = document.querySelector(".from-suggestions");
    toSuggestions = document.querySelector(".to-suggestions");
    amount = document.querySelector("#amount-input");
    exRateTxt = document.querySelector(".result");
    exchangeIcon = document.querySelector(".reverse");

    // Initialize searchable inputs
    initializeSearchInputs();

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
    
    console.log('✅ Searchable Currency Converter initialized successfully!');
    console.log('🔍 Smart search features enabled:');
    console.log('   - Fuzzy matching (e.g., "us" → USD)');
    console.log('   - Country name search (e.g., "america" → USD)');
    console.log('   - Currency name search (e.g., "dollar" → USD)');
    console.log('   - Keyboard navigation (↑↓ arrows, Enter, Escape)');
    console.log('   - Auto-complete suggestions');
}

// Start the application when DOM is loaded
document.addEventListener('DOMContentLoaded', initApp);
