const apiKey = '7e4e5323eb2e1912718807be6853c552';
const locButton = document.querySelector('.loc-button');
const todayInfo = document.querySelector('.today-info');
const todayWeatherIcon = document.querySelector('.today-weather i');
const todayTemp = document.querySelector('.weather-temp');
const daysList = document.querySelector('.days-list');

// Mapping of weather condition codes to icon class names (Depending on OpenWeatherMap API Response)
const weatherIconMapping = {
  '01d': 'sun',
  '01n': 'moon',
  '02d': 'cloud',
  '02n': 'cloud',
  '03d': 'cloud',
  '03n': 'cloud',
  '04d': 'cloud',
  '04n': 'cloud',
  '09d': 'cloud-rain',
  '09n': 'cloud-rain',
  '10d': 'cloud-rain',
  '10n': 'cloud-rain',
  '11d': 'cloud-lightning',
  '11n': 'cloud-lightning',
  '13d': 'cloud-snow',
  '13n': 'cloud-snow',
  '50d': 'water',
  '50n': 'water'
};

function fetchWeatherData(location) {
  const apiUrl = `https://api.openweathermap.org/data/2.5/forecast?q=${location}&appid=${apiKey}&units=metric`;

  // Fetch weather data from OpenWeatherMap API
  fetch(apiUrl)
    .then(response => {
      if (!response.ok) {
        throw new Error('Error fetching weather data');
      }
      return response.json();
    })
    .then(data => {
      if (data.list && data.list.length > 0) {
        const todayWeather = data.list[0].weather[0].description;
        const todayTempValue = `${Math.round(data.list[0].main.temp)}°C`;
        const todayWeatherIconCode = data.list[0].weather[0].icon;

        todayInfo.querySelector('h2').textContent = new Date().toLocaleDateString('en', { weekday: 'long' });
        todayInfo.querySelector('span').textContent = new Date().toLocaleDateString('en', { day: 'numeric', month: 'long', year: 'numeric' });
        todayWeatherIcon.className = `bx bx-${weatherIconMapping[todayWeatherIconCode]}`;
        todayTemp.textContent = todayTempValue;

        // Update location and weather description in the "left-info" section
        const locationElement = document.querySelector('.today-info > div > span');
        locationElement.textContent = `${data.city.name}, ${data.city.country}`;

        const weatherDescriptionElement = document.querySelector('.today-weather > h3');
        weatherDescriptionElement.textContent = todayWeather;

        // Update today's info in the "days-info" section
        const todayPrecipitation = `${data.list[0].pop}%`;
        const todayHumidity = `${data.list[0].main.humidity}%`;
        const todayWindSpeed = `${data.list[0].wind.speed} km/h`;

        const dayInfoContainer = document.querySelector('.day-info');
        dayInfoContainer.innerHTML = `
          <div>
            <span class="title">PRECIPITATION</span>
            <span class="value">${todayPrecipitation}</span>
          </div>
          <div>
            <span class="title">HUMIDITY</span>
            <span class="value">${todayHumidity}</span>
          </div>
          <div>
            <span class="title">WIND SPEED</span>
            <span class="value">${todayWindSpeed}</span>
          </div>
        `;

        // Update next 4 days weather
        const today = new Date();
        const nextDaysData = data.list.slice(1);

        const uniqueDays = new Set();
        let count = 0;
        daysList.innerHTML = '';
        for (const dayData of nextDaysData) {
          const forecastDate = new Date(dayData.dt_txt);
          const dayAbbreviation = forecastDate.toLocaleDateString('en', { weekday: 'short' });
          const dayTemp = `${Math.round(dayData.main.temp)}&deg;C`;
          const iconCode = dayData.weather[0].icon;

          // Ensure the day isn't duplicate and today
          if (!uniqueDays.has(dayAbbreviation) && forecastDate.getDate() !== today.getDate()) {
            uniqueDays.add(dayAbbreviation);
            daysList.innerHTML += `
              <li>
                <i class='bx bx-${weatherIconMapping[iconCode]}'></i>
                <span>${dayAbbreviation}</span>
                <span class="day-temp">${dayTemp}</span>
              </li>
            `;
            count++;
          }

          // Stop after getting 4 distinct days
          if (count === 4) break;
        }
      } else {
        throw new Error('No weather data available');
      }
    })
    .catch(error => {
      console.error('Ups data not found or  ', error);
      alert('Ups data not found or  ' + error.message);
    });
}

// Fetch weather data on document load for default location your chose location
document.addEventListener('DOMContentLoaded', () => {
  const defaultLocation = 'lampung';
  fetchWeatherData(defaultLocation);
});

locButton.addEventListener('click', () => {
  const location = prompt('Please Enter Country/City, example: Canada/Oslo');
  if (!location) return;

  fetchWeatherData(location);
});
