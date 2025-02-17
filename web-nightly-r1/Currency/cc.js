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

//function to get exchange rate from api

async function getExchangeRate() {
    const amountVal = parseFloat(amount.value) || 1; // Ensure amount is a number
    exRateTxt.innerText = "Getting exchange rate...";
    try {
        const response = await fetch(`https://v6.exchangerate-api.com/v6/de1695208ebf652f2f84fe41/latest/${fromCur.value}`);
        const result = await response.json();
        const exchangeRate = result.conversion_rates[toCur.value];
        const totalExRate = (amountVal * exchangeRate).toFixed(2);

        // Format the totalExRate using Intl.NumberFormat
        const formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: toCur.value,
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        });

        exRateTxt.innerText = `${amountVal.toFixed(2)} ${fromCur.value} = ${formatter.format(totalExRate)}`;
    } catch (error) {
        exRateTxt.innerText = "Something went wrong...";
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