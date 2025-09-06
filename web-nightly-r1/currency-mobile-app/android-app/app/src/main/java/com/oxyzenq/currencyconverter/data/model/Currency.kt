package com.oxyzenq.currencyconverter.data.model

import com.google.gson.annotations.SerializedName

data class Currency(
    val code: String,
    val name: String,
    val flag: String
)

data class ConversionRequest(
    @SerializedName("from_currency")
    val fromCurrency: String,
    @SerializedName("to_currency")
    val toCurrency: String,
    val amount: Double
)

data class ConversionResponse(
    val success: Boolean,
    @SerializedName("from_currency")
    val fromCurrency: String,
    @SerializedName("to_currency")
    val toCurrency: String,
    val amount: Double,
    @SerializedName("converted_amount")
    val convertedAmount: Double,
    @SerializedName("exchange_rate")
    val exchangeRate: Double,
    val timestamp: String,
    val error: String?
)

data class ExchangeRatesResponse(
    @SerializedName("base_currency")
    val baseCurrency: String,
    val rates: Map<String, Double>,
    val timestamp: String,
    val source: String
)

data class CurrencyListResponse(
    val currencies: Map<String, String>,
    val count: Int,
    val timestamp: String
)

data class ApiError(
    val error: String,
    val message: String,
    val timestamp: String
)
