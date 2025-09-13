const fromCur = document.querySelector(".from-select");
const toCur = document.querySelector(".to-select");
const getBtn = document.querySelector("form button");
const exIcon = document.querySelector("form .reverse");
const amount = document.querySelector("form input");
const exRateTxt = document.querySelector("form .result");

// Event listener for currency dropdowns (select)

[fromCur, toCur].forEach((select, i) => {
    for (let curCode in Country_List) {
        const selected = (i === 0 && curCode === "USD") || (i === 1 && curCode === "CHF") ? "selected" : "";
        select.insertAdjacentHTML("beforeend", `<option value="${curCode}" ${selected}>${curCode}</option>`);
    }
    select.addEventListener("change", () => {
        const code = select.value;
        const imgTag = select.parentElement.querySelector("img");
        imgTag.src = `https://flagcdn.com/48x36/${Country_List[code].toLowerCase()}.png`;
    });
});

// Configuration
const API_BASE_URL = "http://localhost:8000"; // Local development backend
const JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJvd25lciI6ImtpcmFpIiwiaWF0IjoxNzU3NzU2ODQ1LjA5NDE2NzUsImV4cCI6MTc1Nzc1ODY0NS4wOTQxNjc1LCJwdXJwb3NlIjoiY3VycmVuY3lfYXBpX2FjY2VzcyJ9.xev2_oHMPgkeDX7-NIo0Rj9skLOirSp9g5qDH_29mAQ"; // Valid for 30 minutes

//function to get exchange rate from secure backend API

async function getExchangeRate() {
    const amountVal = parseFloat(amount.value) || 1; // Ensure amount is a number
    exRateTxt.innerText = "Getting exchange rate...";
    
    try {
        // Check if JWT token is configured
        if (JWT_TOKEN === "PASTE_YOUR_JWT_TOKEN_HERE") {
            exRateTxt.innerText = "⚠️ JWT token not configured. Please update the token.";
            console.error("JWT token not configured. Please run generate_token.py and update JWT_TOKEN in cc.js");
            return;
        }

        // Fetch from secure backend API
        const response = await fetch(`${API_BASE_URL}/api/rates/${fromCur.value}?token=${JWT_TOKEN}`);
        
        if (!response.ok) {
            if (response.status === 401) {
                exRateTxt.innerText = "🔒 Token expired. Please generate a new token.";
                console.error("JWT token expired. Please run generate_token.py to get a new token.");
                return;
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
        
        // Check if API returned success
        if (!result.success) {
            throw new Error("API returned unsuccessful response");
        }

        const exchangeRate = result.conversion_rates[toCur.value];
        
        if (!exchangeRate) {
            exRateTxt.innerText = `❌ Exchange rate not available for ${toCur.value}`;
            return;
        }

        const totalExRate = (amountVal * exchangeRate).toFixed(2);

        // Format the totalExRate using Intl.NumberFormat
        const formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: toCur.value,
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        });

        exRateTxt.innerText = `${amountVal.toFixed(2)} ${fromCur.value} = ${formatter.format(totalExRate)}`;
        
        // Log successful request for debugging
        console.log(`✅ Exchange rate fetched: 1 ${fromCur.value} = ${exchangeRate} ${toCur.value}`);
        
    } catch (error) {
        console.error("Exchange rate fetch error:", error);
        
        // Provide user-friendly error messages
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            exRateTxt.innerText = "🌐 Network error. Please check your connection.";
        } else if (error.message.includes('timeout')) {
            exRateTxt.innerText = "⏰ Request timeout. Please try again.";
        } else {
            exRateTxt.innerText = "❌ Something went wrong. Please try again.";
        }
    }
}

//event listener for button and exchange icon click

window.addEventListener("load", getExchangeRate);
getBtn.addEventListener("click", (e) => {
    e.preventDefault();
    getExchangeRate();
});

exIcon.addEventListener("click", () => {
    [fromCur.value, toCur.value] = [toCur.value, fromCur.value];
    [fromCur, toCur].forEach((select) => {
        const code = select.value;
        const imgTag = select.parentElement.querySelector("img");
        imgTag.src = `https://flagcdn.com/48x36/${Country_List[code].toLowerCase()}.png`;        
    });
    getExchangeRate();
});

// Function to reset the form fields //
document.addEventListener('DOMContentLoaded', function() {
    // Event listener restrict value
    document.getElementById('amount-input').addEventListener('input', function() {
        let value = parseFloat(this.value);
        if (value > 1000000) {
            this.value = 1000000;
        } else if (value < 0) {
            this.value = 0;
        }
    });

    // Reset form with button click
    document.getElementById('reset-button').addEventListener('click', function() {
        // Make sure amount input is cleared
        document.getElementById('amount-input').value = '';

        // Reset dropdown values
        const fromSelect = document.querySelector('.from-select');
        const toSelect = document.querySelector('.to-select');
        fromSelect.selectedIndex = 0;
        toSelect.selectedIndex = 0;

        // Reset text in result element
        let resultElement = document.querySelector('.result');
        if (resultElement) {
            resultElement.textContent = 'Getting Exchange Rate...';
        }

        // Reset flags automatically (USD & CHF)
        document.getElementById('from-flag').src = `https://flagcdn.com/48x36/us.png`;
        document.getElementById('to-flag').src = `https://flagcdn.com/48x36/ch.png`;
    });
});


// end //