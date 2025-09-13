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
let fromCur, toCur, amount, exRateTxt, exchangeIcon;

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

// Initialize currency dropdowns
async function populateCurrencyDropdowns() {
    const currencies = await fetchSupportedCurrencies();
    console.log('Populating dropdowns with', currencies.length, 'currencies');

    [fromCur, toCur].forEach(select => {
        select.innerHTML = '';
        currencies.forEach(currency => {
            const option = document.createElement('option');
            option.value = currency.code;
            option.textContent = `${currency.code} - ${currency.name}`;
            select.appendChild(option);
        });
    });

    console.log('Dropdown populated. From currency options:', fromCur.options.length);
    console.log('Dropdown populated. To currency options:', toCur.options.length);

    // Set default values
    fromCur.value = 'USD';
    toCur.value = 'SGD';
    
    // Update flag images
    updateFlag(fromCur);
    updateFlag(toCur);
}

// Update flag image for currency dropdown
function updateFlag(select) {
    const code = select.value;
    const imgTag = select.parentElement.querySelector("img");
    const countryCode = getCountryFromCurrency(code);
    imgTag.src = `https://flagcdn.com/48x36/${countryCode}.png`;
    imgTag.onerror = () => {
        // Fallback to a default flag or hide image
        imgTag.src = `https://flagcdn.com/48x36/un.png`; // UN flag as fallback
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
    const fromCurrency = fromCur.value;
    const toCurrency = toCur.value;
    
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
    const tempValue = fromCur.value;
    fromCur.value = toCur.value;
    toCur.value = tempValue;
    
    updateFlag(fromCur);
    updateFlag(toCur);
    
    // Fetch new rate after swap
    getExchangeRate();
}

// Initialize the application
async function initApp() {
    // Get DOM elements
    fromCur = document.querySelector(".from-select");
    toCur = document.querySelector(".to-select");
    amount = document.querySelector("#amount-input");
    exRateTxt = document.querySelector(".result");
    exchangeIcon = document.querySelector(".reverse");

    // Populate currency dropdowns (wait for completion)
    await populateCurrencyDropdowns();

    // Add event listeners
    [fromCur, toCur].forEach(select => {
        select.addEventListener("change", () => {
            updateFlag(select);
            getExchangeRate();
        });
    });

    exchangeIcon.addEventListener("click", swapCurrencies);
    
    // Add convert button event listener
    const convertButton = document.querySelector("#convert-button");
    if (convertButton) {
        convertButton.addEventListener("click", (e) => {
            e.preventDefault();
            getExchangeRate();
        });
    }

    // Initial display message
    exRateTxt.innerText = "Enter amount and click 'Get Exchange Rate' to convert";
}

// Start the application when DOM is loaded
document.addEventListener('DOMContentLoaded', initApp);
