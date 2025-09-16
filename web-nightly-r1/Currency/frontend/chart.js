// Currency Exchange Rate Chart Module
// Real-time line chart visualization for currency exchange rates

import Chart from 'chart.js/auto';

// Chart configuration and data management
class ExchangeRateChart {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        this.chart = null;
        this.currentRange = '12H';
        this.currentPair = { from: null, to: null };
        
        // Chart data storage - starts empty
        this.chartData = {
            '12H': { labels: [], data: [] },
            '1D': { labels: [], data: [] },
            '1W': { labels: [], data: [] },
            '1M': { labels: [], data: [] },
            '1Y': { labels: [], data: [] },
            '2Y': { labels: [], data: [] },
            '5Y': { labels: [], data: [] },
            '10Y': { labels: [], data: [] }
        };
        
        this.initializeChart();
        this.setupEventListeners();
    }
    
    initializeChart() {
        const data = this.chartData[this.currentRange];
        
        this.chart = new Chart(this.ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: this.currentPair.from && this.currentPair.to ? `${this.currentPair.from} to ${this.currentPair.to}` : 'Exchange Rate',
                    data: data.data,
                    borderColor: '#4285f4',
                    backgroundColor: 'rgba(66, 133, 244, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#4285f4',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    pointHoverBackgroundColor: '#4285f4',
                    pointHoverBorderColor: '#ffffff',
                    pointHoverBorderWidth: 3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    intersect: false,
                    mode: 'index'
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        // Start disabled when there is no data
                        enabled: false,
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleColor: '#ffffff',
                        bodyColor: '#ffffff',
                        borderColor: '#4285f4',
                        borderWidth: 1,
                        cornerRadius: 8,
                        displayColors: false,
                        callbacks: {
                            title: function(context) {
                                return `Time: ${context[0].label}`;
                            },
                            label: function(context) {
                                return `Rate: ${context.parsed.y.toFixed(6)} ${this.currentPair.to}`;
                            }.bind(this)
                        }
                    }
                },
                layout: {
                    padding: {
                        top: 10,
                        right: 5,
                        bottom: 10,
                        left: 5
                    }
                },
                scales: {
                    x: {
                        display: true,
                        title: {
                            // hidden by default when no data
                            display: false,
                            text: 'Time',
                            color: '#94a3b8',
                            font: {
                                family: 'Inter',
                                size: 12,
                                weight: '500'
                            }
                        },
                        grid: {
                            color: 'rgba(148, 163, 184, 0.1)',
                            drawBorder: false,
                            // hidden by default when no data
                            display: false
                        },
                        ticks: {
                            color: '#94a3b8',
                            font: {
                                family: 'Inter',
                                size: 11
                            },
                            // hidden by default when no data
                            display: false
                        }
                    },
                    y: {
                        display: true,
                        title: {
                            // hidden by default when no data
                            display: false,
                            text: this.currentPair.to ? `Value (${this.currentPair.to})` : 'Value',
                            color: '#94a3b8',
                            font: {
                                family: 'Inter',
                                size: 12,
                                weight: '500'
                            }
                        },
                        grid: {
                            color: 'rgba(148, 163, 184, 0.1)',
                            drawBorder: false,
                            // hidden by default when no data
                            display: false
                        },
                        ticks: {
                            color: '#94a3b8',
                            font: {
                                family: 'Inter',
                                size: 11
                            },
                            // hidden by default when no data
                            display: false,
                            callback: function(value) {
                                return value.toFixed(6);
                            }
                        }
                    }
                },
                animation: {
                    duration: 1000,
                    easing: 'easeInOutQuart'
                }
            }
        });
    }
    
    setupEventListeners() {
        // Time range button listeners
        const timeRangeBtns = document.querySelectorAll('.time-range-btn');
        timeRangeBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                // Remove active class from all buttons
                timeRangeBtns.forEach(b => b.classList.remove('active'));
                // Add active class to clicked button
                e.target.classList.add('active');
                
                // Update chart data
                this.currentRange = e.target.dataset.range;
                this.updateChart();
            });
        });
    }
    
    updateChart() {
        const data = this.chartData[this.currentRange];
        
        // Update chart data with animation
        this.chart.data.labels = data.labels;
        this.chart.data.datasets[0].data = data.data;
        this.chart.data.datasets[0].label = `${this.currentPair.from} to ${this.currentPair.to}`;
        
        // Update titles and toggle axes visibility depending on data
        const hasData = (data.labels && data.labels.length > 0) && (data.data && data.data.length > 0);
        
        // Axis titles
        this.chart.options.scales.y.title.text = `Value (${this.currentPair.to})`;
        this.chart.options.scales.x.title.display = hasData;
        this.chart.options.scales.y.title.display = hasData;
        
        // Grid and ticks visibility
        this.chart.options.scales.x.grid.display = hasData;
        this.chart.options.scales.y.grid.display = hasData;
        this.chart.options.scales.x.ticks.display = hasData;
        this.chart.options.scales.y.ticks.display = hasData;
        
        // Tooltip visibility
        this.chart.options.plugins.tooltip.enabled = hasData;
        
        // Update current rate display with dynamic precision
        const currentRateElement = document.querySelector('.chart-current-rate');
        if (currentRateElement) {
            // Use higher precision for very small numbers, standard for larger ones
            const last = data.data[data.data.length - 1];
            const precision = last < 0.001 ? 8 : last < 1 ? 6 : 4;
            currentRateElement.textContent = data.data.length > 0 ? last.toFixed(precision) : '0.000000';
        }
        
        this.chart.update('active');
    }
    
    updateCurrencyPair(fromCurrency, toCurrency) {
        this.currentPair = { from: fromCurrency, to: toCurrency };
        
        // Update currency pair display with input amount if available
        const fromDisplay = document.querySelector('.chart-from');
        const toDisplay = document.querySelector('.chart-to');
        
        if (fromDisplay) {
            // Use stored input amount if available, otherwise default to 1
            const displayAmount = this.inputAmount || 1;
            fromDisplay.textContent = `${displayAmount} ${fromCurrency}`;
        }
        if (toDisplay) {
            toDisplay.textContent = toCurrency;
        }
        
        // Update chart
        this.updateChart();
    }
    
    // Generate realistic historical data based on current rate
    generateHistoricalData(currentRate, timeRange) {
        const dataPoints = {
            '12H': { count: 12, interval: 'hour', label: (i) => `${23-i}:00` },
            '1D': { count: 24, interval: 'hour', label: (i) => `${23-i}:00` },
            '1W': { count: 7, interval: 'day', label: (i) => {
                const date = new Date();
                date.setDate(date.getDate() - (6-i));
                return date.toLocaleDateString('en-US', { weekday: 'short' });
            }},
            '1M': { count: 30, interval: 'day', label: (i) => {
                const date = new Date();
                date.setDate(date.getDate() - (29-i));
                return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
            }},
            '1Y': { count: 12, interval: 'month', label: (i) => {
                const date = new Date();
                date.setMonth(date.getMonth() - (11-i));
                return date.toLocaleDateString('en-US', { month: 'short', year: '2-digit' });
            }},
            '2Y': { count: 24, interval: 'month', label: (i) => {
                const date = new Date();
                date.setMonth(date.getMonth() - (23-i));
                return date.toLocaleDateString('en-US', { month: 'short', year: '2-digit' });
            }},
            '5Y': { count: 60, interval: 'month', label: (i) => {
                const date = new Date();
                date.setMonth(date.getMonth() - (59-i));
                return date.toLocaleDateString('en-US', { month: 'short', year: '2-digit' });
            }},
            '10Y': { count: 120, interval: 'month', label: (i) => {
                const date = new Date();
                date.setMonth(date.getMonth() - (119-i));
                return date.toLocaleDateString('en-US', { month: 'short', year: '2-digit' });
            }}
        };
        
        const config = dataPoints[timeRange];
        if (!config) return { labels: [], data: [] };
        
        const labels = [];
        const data = [];
        
        // Generate realistic fluctuations around current rate
        const volatility = {
            '12H': 0.002, // 0.2% volatility
            '1D': 0.005,  // 0.5% volatility
            '1W': 0.015,  // 1.5% volatility
            '1M': 0.03,   // 3% volatility
            '1Y': 0.08,   // 8% volatility
            '2Y': 0.12,   // 12% volatility
            '5Y': 0.20,   // 20% volatility
            '10Y': 0.35   // 35% volatility
        }[timeRange];
        
        let previousRate = currentRate * (0.98 + Math.random() * 0.04); // Start within ±2%
        
        for (let i = 0; i < config.count; i++) {
            labels.push(config.label(i));
            
            // Generate realistic price movement
            const change = (Math.random() - 0.5) * volatility * 2;
            const trend = (currentRate - previousRate) * 0.1; // Slight trend toward current rate
            previousRate = previousRate * (1 + change + trend);
            
            // Ensure we end close to current rate for recent data
            if (i === config.count - 1) {
                previousRate = currentRate;
            }
            
            data.push(previousRate);
        }
        
        return { labels, data };
    }
    
    // Method to add conversion result to chart and generate historical data
    addConversionData(fromCurrency, toCurrency, exchangeRate, amount, convertedAmount) {
        // Update current pair
        this.currentPair = { from: fromCurrency, to: toCurrency };
        
        // Store the input amount for display
        this.inputAmount = amount;
        
        // Generate historical data for all time ranges based on current rate
        Object.keys(this.chartData).forEach(range => {
            const historicalData = this.generateHistoricalData(exchangeRate, range);
            this.chartData[range] = historicalData;
        });
        
        // Update currency pair display with input amount
        const fromDisplay = document.querySelector('.chart-from');
        const toDisplay = document.querySelector('.chart-to');
        
        if (fromDisplay) fromDisplay.textContent = `${amount} ${fromCurrency}`;
        if (toDisplay) toDisplay.textContent = `${toCurrency} ${convertedAmount.toFixed(4)}`;
        
        this.updateChart();
        
        if (window.APP_CONFIG?.DEBUG_MODE) {
            console.log(`📊 Chart updated with historical data: ${amount} ${fromCurrency} = ${convertedAmount.toFixed(2)} ${toCurrency} (rate: ${exchangeRate.toFixed(6)})`);
        }
    }
    
    // Clear all chart data
    clearChartData() {
        Object.keys(this.chartData).forEach(range => {
            this.chartData[range].labels = [];
            this.chartData[range].data = [];
        });
        this.updateChart();
    }
    
    destroy() {
        if (this.chart) {
            this.chart.destroy();
        }
    }
}

// Initialize chart when DOM is loaded
let exchangeChart = null;

function initializeExchangeChart() {
    const chartCanvas = document.getElementById('exchangeRateChart');
    if (chartCanvas && !exchangeChart) {
        exchangeChart = new ExchangeRateChart('exchangeRateChart');
        
        // Make chart globally accessible for reset function
        window.exchangeChart = exchangeChart;
        
        // Start real-time simulation for demo
        if (window.APP_CONFIG?.DEBUG_MODE) {
            console.log('🔄 Starting chart real-time simulation');
            exchangeChart.startRealTimeSimulation();
        }
    }
}

// Function to update chart when currency pair changes
function updateChartCurrencyPair(fromCurrency, toCurrency) {
    if (exchangeChart) {
        exchangeChart.updateCurrencyPair(fromCurrency, toCurrency);
    }
}

// Function to add conversion data to chart
function addChartConversionData(fromCurrency, toCurrency, exchangeRate, amount, convertedAmount) {
    if (exchangeChart) {
        exchangeChart.addConversionData(fromCurrency, toCurrency, exchangeRate, amount, convertedAmount);
    }
}

// Export functions for use in main.js
export { initializeExchangeChart, updateChartCurrencyPair, addChartConversionData, ExchangeRateChart };
