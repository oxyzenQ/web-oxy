document.addEventListener('DOMContentLoaded', () => {
    const pathname = window.location.pathname;
    const token = localStorage.getItem('authToken');

    // Utility function for toggling password visibility
    function togglePasswordVisibility(buttonId, passwordFieldId) {
        const button = document.getElementById(buttonId);
        const field = document.getElementById(passwordFieldId);
        if (button && field) {
            button.addEventListener('click', () => {
                const isPasswordVisible = field.type === 'text';
                field.type = isPasswordVisible ? 'password' : 'text';
                button.textContent = isPasswordVisible ? 'Show Password' : 'Hide Password';
            });
        }
    }

    // Initialize password toggle functionality for login and registration forms
    togglePasswordVisibility('show-pw-login', 'login-password');
    togglePasswordVisibility('show-pw-register', 'register-password');

    // Function to show notifications
    function showNotification(message, type) {
        const successMessage = document.getElementById('success-message');
        const errorMessage = document.getElementById('error-message');
        
        if (type === 'success') {
            successMessage.textContent = message;
            successMessage.style.display = 'block';
            errorMessage.style.display = 'none';
        } else {
            errorMessage.textContent = message;
            errorMessage.style.display = 'block';
            successMessage.style.display = 'none';
        }
    }

    // Function to handle BMI calculations
    function handleBMICalculation() {
        const bmiForm = document.getElementById('bmi-form');
        if (bmiForm) {
            bmiForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const age = document.getElementById('age').value;
                const gender = document.querySelector('input[name="gender"]:checked')?.value;
                const height = document.getElementById('height').value;
                const weight = document.getElementById('weight').value;

                if (!age || !gender || !height || !weight) {
                    alert('Please fill in all fields');
                    return;
                }

                const heightInMeters = height / 100;
                const bmi = weight / (heightInMeters * heightInMeters);
                document.getElementById('bmi-output').textContent = bmi.toFixed(2);

                const result = await fetch('/api/user/calc', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({ bmi, date: new Date() })
                }).then(response => response.json());

                if (!result.success) {
                    console.error('Failed to save BMI:', result.error);
                }
            });
        }
    }

    handleBMICalculation();

    // Reset button functionality
    const form = document.getElementById('bmi-form');
    const resetButton = document.getElementById('reset-button');
    const bmiResult = document.getElementById('bmi-output');
    const status = document.getElementById('status');

    if (resetButton) {
        resetButton.addEventListener('click', () => {
            // Clear form fields
            form.reset();
    
            // Clear BMI result and status
            bmiResult.textContent = '0';
            status.textContent = '--';
        });
    }
});

// Randomize background position
let isScrolling = false;

function randomizeBackgroundPosition() {
    if (!isScrolling) {
        isScrolling = true;
        requestAnimationFrame(() => {
            const xPos = Math.floor(Math.random() * 101); // Between 0% and 100%
            const yPos = Math.floor(Math.random() * 101); // Between 0% and 100%
            document.body.style.backgroundPosition = `${xPos}% ${yPos}%`;
            isScrolling = false;
        });
    }
}

// Randomize background on page load and scroll
window.addEventListener('load', randomizeBackgroundPosition);
window.addEventListener('scroll', randomizeBackgroundPosition);

// Function to calculate BMI and display result
async function calculateBMI() {
    const age = document.getElementById('age').value;
    const gender = document.querySelector('input[name="gender"]:checked');
    const height = document.getElementById('height').value;
    const weight = document.getElementById('weight').value;

    if (age && gender && height && weight) {
        const heightInMeters = height / 100;
        const bmi = weight / (heightInMeters * heightInMeters);
        let bmiCategory = '';
        let categoryClass = '';

        if (bmi < 18.5) {
            bmiCategory = 'Underweight';
            categoryClass = 'underweight';
        } else if (bmi >= 18.5 && bmi < 24.9) {
            bmiCategory = 'Healthy weight';
            categoryClass = 'healthy';
        } else if (bmi >= 25 && bmi < 29.9) {
            bmiCategory = 'Overweight';
            categoryClass = 'overweight';
        } else {
            bmiCategory = 'Obesity';
            categoryClass = 'obesity';
        }

        const bmiOutput = document.getElementById('bmi-output');
        bmiOutput.textContent = `${bmi.toFixed(2)} (${bmiCategory})`;
        bmiOutput.className = categoryClass;

    } else {
        alert('Please fill out all fields.');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const scrollUpLink = document.getElementById('scroll-up-link');
    
    if (scrollUpLink) {
        scrollUpLink.addEventListener('click', (event) => {
            event.preventDefault(); // Prevent default anchor behavior
            document.querySelector(scrollUpLink.getAttribute('href')).scrollIntoView({
                behavior: 'smooth'
            });
        });
    }
});
