document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('register-form');
    const loginForm = document.getElementById('login-form');

    if (registerForm) {
        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const name = document.getElementById('register-name').value;
            const email = document.getElementById('register-email').value;
            const password = document.getElementById('register-password').value;

            try {
                const response = await fetch('/api/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, email, password })
                });

                const result = await response.json();
                if (result.success) {
                    showNotification('Registration successful!', 'success');
                    setTimeout(() => {
                        window.location.href = 'https://kernel.org';
                    }, 2000); // Redirect after 2 seconds
                } else {
                    showNotification(result.message || 'Registration failed!', 'error');
                }
            } catch (error) {
                showNotification('An error occurred. Please try again.', 'error');
            }
        });
    }

    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;

            try {
                const response = await fetch('/api/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                const result = await response.json();
                if (result.success) {
                    showNotification('Login successful!', 'success');
                    setTimeout(() => {
                        window.location.href = 'https://kernel.org';
                    }, 2000); // Redirect after 2 seconds
                } else {
                    showNotification(result.message || 'Login failed!', 'error');
                }
            } catch (error) {
                showNotification('An error occurred. Please try again.', 'error');
            }
        });
    }
});

// Function to show notification
const showNotification = (message, type) => {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);
    setTimeout(() => {
        notification.remove();
    }, 5000); // Remove notification after 5 seconds
};