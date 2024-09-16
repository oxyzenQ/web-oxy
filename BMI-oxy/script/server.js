require('dotenv').config(); // Load environment variables from .env file

const express = require('express');
const mongoose = require('mongoose');
const bcrypt = require('bcrypt');
const bodyParser = require('body-parser');
const cors = require('cors');
const path = require('path');
const jwt = require('jsonwebtoken');

const app = express();
const port = process.env.PORT || 3000;

// Define the schema
const bmiSchema = new mongoose.Schema({
    userId: mongoose.Schema.Types.ObjectId,
    age: Number,
    gender: String,
    height: Number,
    weight: Number,
    bmi: Number,
    date: { type: Date, default: Date.now }
});

const Bmi = mongoose.model('Bmi', bmiSchema);

const UserSchema = new mongoose.Schema({
  name: String,
  age: Number,
  email: String,
  password: String,
  hobbies: String, // Ensure this field is defined
  bmiHistory: [{ date: Date, bmi: Number }]
});
const User = mongoose.model('User', UserSchema);

// Middleware
app.use(bodyParser.json());
app.use(cors());
app.use(express.static(path.join(__dirname))); // Serves static files

// MongoDB Atlas URI
const mongoURI = process.env.MONGO_URI;
const JWT_SECRET = process.env.JWT_SECRET;

// Connect to MongoDB
mongoose.connect(mongoURI)
  .then(() => console.log('MongoDB connected'))
  .catch(err => console.log(err));

// Connect to MongoDB
mongoose.connect(mongoURI, { useNewUrlParser: true, useUnifiedTopology: true })
    .then(() => console.log('MongoDB connected'))
    .catch(err => console.log(err));

// Middleware to verify JWT
const authenticateToken = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1];
    if (token == null) return res.sendStatus(401);

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) return res.sendStatus(403);
        req.user = user;
        next();
    });
};

// POST endpoint for BMI
app.post('/api/bmi', authenticateToken, async (req, res) => {
    try {
        const { age, gender, height, weight, bmi } = req.body;
        const userId = req.user.id;

        // Simpan BMI ke koleksi Bmi
        const newBmi = new Bmi({ userId, age, gender, height, weight, bmi });
        await newBmi.save();

        // Tambahkan BMI ke riwayat pengguna
        const user = await User.findById(userId);
        if (user) {
            user.bmiHistory.push({ date: new Date(), bmi });
            await user.save();
        }

        res.status(201).json({ message: 'BMI data saved successfully' });
    } catch (error) {
        res.status(500).json({ error: 'Error saving BMI data' });
    }
});

// Routes for static HTML files
app.get('/', (req, res) => res.sendFile(path.join(__dirname, 'home.html')));
app.get('/sign.html', (req, res) => res.sendFile(path.join(__dirname, 'sign.html')));
app.get('/signup.html', (req, res) => res.sendFile(path.join(__dirname, 'signup.html')));
app.get('/profile.html', authenticateToken, (req, res) => res.sendFile(path.join(__dirname, 'profile.html')));

// User registration route
app.post('/signup', async (req, res) => {
    const { name, email, age, password, hobbies } = req.body;
    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        const newUser = new User({ 
            name, 
            email,
            age, 
            password: hashedPassword, 
            hobbies 
        });
        await newUser.save();
        res.status(201).json({ message: 'User registered', redirect: '/sign.html' });
    } catch (error) {
        res.status(500).json({ error: 'Error registering user' });
    }
});

// User login route
app.post('/sign', async (req, res) => {
    const { email, password } = req.body;
    try {
        const user = await User.findOne({ email });
        if (user && await bcrypt.compare(password, user.password)) {
            const token = jwt.sign({ id: user._id, email: user.email }, JWT_SECRET, { expiresIn: '1h' });
            res.status(200).json({ message: 'Login successful', token });
        } else {
            res.status(401).json({ error: 'Invalid credentials' });
        }
    } catch (error) {
        res.status(500).json({ error: 'Error logging in' });
    }
});

// Rute untuk mendapatkan profil pengguna
app.get('/api/user/profile', authenticateToken, async (req, res) => {
  try {
      const user = await User.findById(req.user.id);
      if (user) {
          res.json({
              name: user.name,
              age: user.age,
              email: user.email,
              hobbies: user.hobbies, // Ensure hobbies is included
              bmiHistory: user.bmiHistory
          });
      } else {
          res.status(404).json({ error: 'User not found' });
      }
  } catch (error) {
      res.status(500).json({ error: 'Error fetching user profile' });
  }
});

// Get BMI history route
app.get('/api/bmi/history', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        const bmiHistory = await Bmi.find({ userId }).sort({ date: -1 });
        res.status(200).json(bmiHistory);
    } catch (error) {
        res.status(500).json({ error: 'Error fetching BMI history' });
    }
});

// Start server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});