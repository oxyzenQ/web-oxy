const fs = require('fs');
const path = require('path');
const dataFilePath = path.join(__dirname, '../data/users.json');

// Read JSON data from file
const readData = () => {
    const data = fs.readFileSync(dataFilePath, 'utf8');
    return JSON.parse(data);
};

// Write JSON data to file
const writeData = (data) => {
    fs.writeFileSync(dataFilePath, JSON.stringify(data, null, 2));
};

// Add a new user to the JSON file
const addUser = (user) => {
    const data = readData();
    data.push(user);
    writeData(data);
};

// Find a user by email
const findUserByEmail = (email) => {
    const data = readData();
    return data.find(user => user.email === email);
};

module.exports = { addUser, findUserByEmail };