document.addEventListener('DOMContentLoaded', async () => {
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

    // Utility function for making API requests with error handling
    async function fetchData(url, options) {
        try {
            const response = await fetch(url, options);
            if (!response.ok) {
                throw new Error(await response.text());
            }
            return response.json();
        } catch (error) {
            console.error('API request failed:', error);
            alert('An error occurred. Please try again later.');
        }
    }

    // Function to update user profile information
    async function updateUserProfile() {
        const response = await fetch('/api/user/profile', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const userProfile = await response.json();
            document.getElementById('user-name').textContent = userProfile.name || 'N/A';
            document.getElementById('user-age').textContent = userProfile.age || 'N/A';
            document.getElementById('user-email').textContent = userProfile.email || 'N/A';
            document.getElementById('user-hobbies').textContent = userProfile.hobbies || 'N/A';

            // Update BMI history table
            const historyTableBody = document.querySelector('#history-table tbody');
            historyTableBody.innerHTML = ''; // Clear existing rows
            userProfile.bmiHistory.forEach(record => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${new Date(record.date).toLocaleDateString()}</td>
                    <td>${record.bmi.toFixed(2)}</td>
                `;
                historyTableBody.appendChild(row);
            });
        } else {
            console.error('Failed to fetch user profile');
        }
    }

    // Function to handle login
    function handleLogin() {
        togglePasswordVisibility('show-pw-login', 'login-password');
        const loginForm = document.getElementById('login-form-fields');
        if (loginForm) {
            loginForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const email = document.getElementById('login-email').value;
                const password = document.getElementById('login-password').value;
                const result = await fetchData('/sign', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });
                if (result?.token) {
                    localStorage.setItem('authToken', result.token);
                    window.location.href = 'profile.html';
                } else {
                    alert(result?.error || 'Login failed.');
                }
            });
        }

        const registerButton = document.getElementById('register-btn');
        if (registerButton) {
            registerButton.addEventListener('click', () => {
                window.location.href = 'signup.html';
            });
        }
    }

    // Function to handle registration
    function handleRegistration() {
        togglePasswordVisibility('show-pw-register', 'register-password');
        const registerForm = document.getElementById('register-form-fields');
        if (registerForm) {
            registerForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const name = document.getElementById('register-username').value;
                const email = document.getElementById('register-email').value;
                const age = document.getElementById('register-age').value;
                const password = document.getElementById('register-password').value;
                const hobbies = document.getElementById('register-hobbies').value;
                const result = await fetchData('/signup', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, email, age, password, hobbies })
                });
                if (result?.redirect) {
                    window.location.href = result.redirect;
                } else {
                    alert(result?.error || 'Registration failed.');
                }
            });
        }

        const backToLoginBtn = document.getElementById('back-to-login');
        if (backToLoginBtn) {
            backToLoginBtn.addEventListener('click', () => {
                window.location.href = 'sign.html';
            });
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

                const result = await fetchData('/api/user/calc', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({ bmi, date: new Date() })
                });

                if (!result?.success) {
                    console.error('Failed to save BMI:', result?.error);
                }
            });
        }
    }

    // Routing logic based on the pathname
    switch (pathname) {
        case '/profile.html':
            if (!token) {
                window.location.href = 'sign.html';
            } else {
                await updateUserProfile();
            }
            break;
        case '/sign.html':
            handleLogin();
            break;
        case '/signup.html':
            handleRegistration();
            break;
        case '/calc.html':
            handleBMICalculation();
            break;
        default:
            console.warn('No specific handling for this page.');
    }
});

let isScrolling = false;

function randomizeBackgroundPosition() {
    if (!isScrolling) {
        isScrolling = true;
        requestAnimationFrame(() => {
            // Generate random percentages for background position
            const xPos = Math.floor(Math.random() * 101); // Between 0% and 100%
            const yPos = Math.floor(Math.random() * 101); // Between 0% and 100%

            // Apply the random background position
            document.body.style.backgroundPosition = `${xPos}% ${yPos}%`;

            isScrolling = false;
        });
    }
}

// Randomize background on page load
window.onload = randomizeBackgroundPosition;

// Randomize background on scroll
window.addEventListener('scroll', randomizeBackgroundPosition);

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

        // Determine BMI category
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

        // Display BMI result
        const bmiOutput = document.getElementById('bmi-output');
        bmiOutput.textContent = `${bmi.toFixed(2)} (${bmiCategory})`;
        bmiOutput.className = categoryClass;

    } else {
        alert('Please fill out all fields.');
    }
    
}