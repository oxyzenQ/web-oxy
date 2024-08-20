const express = require('express');
const bodyParser = require('body-parser');
const { addUser, findUserByEmail } = require('./js/database');

const app = express();
const port = 3000;

app.use(bodyParser.json());
app.use(express.static('.')); // Serve static files from the root directory

// Handle user registration
app.post('/api/register', (req, res) => {
    const { name, email, password } = req.body;
    if (findUserByEmail(email)) {
        return res.json({ success: false, message: 'User already exists' });
    }
    addUser({ name, email, password });
    res.json({ success: true });
});

// Handle user login
app.post('/api/login', (req, res) => {
    const { email, password } = req.body;
    const user = findUserByEmail(email);
    if (user && user.password === password) {
        res.json({ success: true });
    } else {
        res.json({ success: false, message: 'Invalid credentials' });
    }
});

app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
