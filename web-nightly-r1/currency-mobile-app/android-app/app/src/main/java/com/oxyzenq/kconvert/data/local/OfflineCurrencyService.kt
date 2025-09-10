/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local

import com.oxyzenq.kconvert.data.model.ConversionResponse
import com.oxyzenq.kconvert.data.model.Currency
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineCurrencyService @Inject constructor() {
    
    // Embedded exchange rates (USD as base) - updated periodically
    private val exchangeRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.85,
        "GBP" to 0.73,
        "JPY" to 110.0,
        "AUD" to 1.35,
        "CAD" to 1.25,
        "CHF" to 0.92,
        "CNY" to 6.45,
        "SEK" to 8.60,
        "NZD" to 1.42,
        "MXN" to 20.15,
        "SGD" to 1.35,
        "HKD" to 7.80,
        "NOK" to 8.45,
        "KRW" to 1180.0,
        "TRY" to 8.50,
        "RUB" to 75.0,
        "INR" to 74.5,
        "BRL" to 5.20,
        "ZAR" to 14.8,
        "AED" to 3.67,
        "SAR" to 3.75,
        "QAR" to 3.64,
        "KWD" to 0.30,
        "BHD" to 0.38,
        "OMR" to 0.38,
        "JOD" to 0.71,
        "LBP" to 1507.5,
        "EGP" to 15.7,
        "MAD" to 8.95,
        "DZD" to 134.0,
        "TND" to 2.75,
        "LYD" to 1.38,
        "SDG" to 55.0,
        "ETB" to 43.5,
        "KES" to 108.0,
        "UGX" to 3550.0,
        "TZS" to 2310.0,
        "RWF" to 1000.0,
        "GHS" to 6.10,
        "NGN" to 411.0,
        "XOF" to 558.0,
        "XAF" to 558.0,
        "MGA" to 4000.0,
        "MUR" to 40.5,
        "SCR" to 13.4,
        "MWK" to 815.0,
        "ZMW" to 16.2,
        "BWP" to 11.0,
        "SZL" to 14.8,
        "LSL" to 14.8,
        "NAD" to 14.8
    )
    
    private val currencyNames = mapOf(
        "USD" to "US Dollar",
        "EUR" to "Euro",
        "GBP" to "British Pound",
        "JPY" to "Japanese Yen",
        "AUD" to "Australian Dollar",
        "CAD" to "Canadian Dollar",
        "CHF" to "Swiss Franc",
        "CNY" to "Chinese Yuan",
        "SEK" to "Swedish Krona",
        "NZD" to "New Zealand Dollar",
        "MXN" to "Mexican Peso",
        "SGD" to "Singapore Dollar",
        "HKD" to "Hong Kong Dollar",
        "NOK" to "Norwegian Krone",
        "KRW" to "South Korean Won",
        "TRY" to "Turkish Lira",
        "RUB" to "Russian Ruble",
        "INR" to "Indian Rupee",
        "BRL" to "Brazilian Real",
        "ZAR" to "South African Rand",
        "AED" to "UAE Dirham",
        "SAR" to "Saudi Riyal",
        "QAR" to "Qatari Riyal",
        "KWD" to "Kuwaiti Dinar",
        "BHD" to "Bahraini Dinar",
        "OMR" to "Omani Rial",
        "JOD" to "Jordanian Dinar",
        "LBP" to "Lebanese Pound",
        "EGP" to "Egyptian Pound",
        "MAD" to "Moroccan Dirham",
        "DZD" to "Algerian Dinar",
        "TND" to "Tunisian Dinar",
        "LYD" to "Libyan Dinar",
        "SDG" to "Sudanese Pound",
        "ETB" to "Ethiopian Birr",
        "KES" to "Kenyan Shilling",
        "UGX" to "Ugandan Shilling",
        "TZS" to "Tanzanian Shilling",
        "RWF" to "Rwandan Franc",
        "GHS" to "Ghanaian Cedi",
        "NGN" to "Nigerian Naira",
        "XOF" to "West African CFA Franc",
        "XAF" to "Central African CFA Franc",
        "MGA" to "Malagasy Ariary",
        "MUR" to "Mauritian Rupee",
        "SCR" to "Seychellois Rupee",
        "MWK" to "Malawian Kwacha",
        "ZMW" to "Zambian Kwacha",
        "BWP" to "Botswanan Pula",
        "SZL" to "Swazi Lilangeni",
        "LSL" to "Lesotho Loti",
        "NAD" to "Namibian Dollar"
    )
    
    private val currencyToCountry = mapOf(
        "USD" to "US", "EUR" to "EU", "GBP" to "GB", "JPY" to "JP",
        "AUD" to "AU", "CAD" to "CA", "CHF" to "CH", "CNY" to "CN",
        "SEK" to "SE", "NZD" to "NZ", "MXN" to "MX", "SGD" to "SG",
        "HKD" to "HK", "NOK" to "NO", "KRW" to "KR", "TRY" to "TR",
        "RUB" to "RU", "INR" to "IN", "BRL" to "BR", "ZAR" to "ZA",
        "AED" to "AE", "SAR" to "SA", "QAR" to "QA", "KWD" to "KW",
        "BHD" to "BH", "OMR" to "OM", "JOD" to "JO", "LBP" to "LB",
        "EGP" to "EG", "MAD" to "MA", "DZD" to "DZ", "TND" to "TN",
        "LYD" to "LY", "SDG" to "SD", "ETB" to "ET", "KES" to "KE",
        "UGX" to "UG", "TZS" to "TZ", "RWF" to "RW", "GHS" to "GH",
        "NGN" to "NG", "XOF" to "SN", "XAF" to "CM", "MGA" to "MG",
        "MUR" to "MU", "SCR" to "SC", "MWK" to "MW", "ZMW" to "ZM",
        "BWP" to "BW", "SZL" to "SZ", "LSL" to "LS", "NAD" to "NA"
    )
    
    suspend fun getCurrencies(): Result<List<Currency>> {
        // Simulate loading delay
        delay(500)
        
        val currencies = exchangeRates.keys.map { code ->
            Currency(
                code = code,
                name = currencyNames[code] ?: code,
                flag = getFlagUrl(code)
            )
        }.sortedBy { it.name }
        
        return Result.success(currencies)
    }
    
    suspend fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Result<ConversionResponse> {
        // Simulate conversion delay
        delay(300)
        
        val fromRate = exchangeRates[fromCurrency]
        val toRate = exchangeRates[toCurrency]
        
        if (fromRate == null || toRate == null) {
            return Result.failure(Exception("Currency not supported"))
        }
        
        // Convert to USD first, then to target currency
        val usdAmount = amount / fromRate
        val convertedAmount = usdAmount * toRate
        val exchangeRate = toRate / fromRate
        
        val response = ConversionResponse(
            success = true,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency,
            amount = amount,
            convertedAmount = String.format("%.2f", convertedAmount).toDouble(),
            exchangeRate = String.format("%.6f", exchangeRate).toDouble(),
            timestamp = System.currentTimeMillis().toString(),
            error = null
        )
        
        return Result.success(response)
    }
    
    suspend fun getExchangeRates(baseCurrency: String): Result<Map<String, Double>> {
        delay(200)
        
        val baseRate = exchangeRates[baseCurrency]
            ?: return Result.failure(Exception("Base currency not supported"))
        
        val rates = exchangeRates.mapValues { (_, rate) ->
            rate / baseRate
        }
        
        return Result.success(rates)
    }
    
    private fun getFlagUrl(currencyCode: String): String {
        val countryCode = currencyToCountry[currencyCode] ?: "XX"
        return "https://flagcdn.com/24x18/${countryCode.lowercase()}.png"
    }
}
