# Enhanced Currency Data with 100+ Countries Support
# Production-grade currency mapping with comprehensive country data

SUPPORTED_CURRENCIES = [
    # Major World Currencies (G20 + Major Trading Partners)
    {'code': 'USD', 'name': 'US Dollar', 'country': 'us', 'region': 'North America', 'priority': 1},
    {'code': 'EUR', 'name': 'Euro', 'country': 'eu', 'region': 'Europe', 'priority': 1},
    {'code': 'GBP', 'name': 'British Pound', 'country': 'gb', 'region': 'Europe', 'priority': 1},
    {'code': 'JPY', 'name': 'Japanese Yen', 'country': 'jp', 'region': 'Asia', 'priority': 1},
    {'code': 'CNY', 'name': 'Chinese Yuan', 'country': 'cn', 'region': 'Asia', 'priority': 1},
    {'code': 'AUD', 'name': 'Australian Dollar', 'country': 'au', 'region': 'Oceania', 'priority': 1},
    {'code': 'CAD', 'name': 'Canadian Dollar', 'country': 'ca', 'region': 'North America', 'priority': 1},
    {'code': 'CHF', 'name': 'Swiss Franc', 'country': 'ch', 'region': 'Europe', 'priority': 1},
    {'code': 'SGD', 'name': 'Singapore Dollar', 'country': 'sg', 'region': 'Asia', 'priority': 1},
    {'code': 'HKD', 'name': 'Hong Kong Dollar', 'country': 'hk', 'region': 'Asia', 'priority': 1},
    
    # Major Asian Currencies
    {'code': 'INR', 'name': 'Indian Rupee', 'country': 'in', 'region': 'Asia', 'priority': 2},
    {'code': 'KRW', 'name': 'South Korean Won', 'country': 'kr', 'region': 'Asia', 'priority': 2},
    {'code': 'IDR', 'name': 'Indonesian Rupiah', 'country': 'id', 'region': 'Asia', 'priority': 2},
    {'code': 'THB', 'name': 'Thai Baht', 'country': 'th', 'region': 'Asia', 'priority': 2},
    {'code': 'MYR', 'name': 'Malaysian Ringgit', 'country': 'my', 'region': 'Asia', 'priority': 2},
    {'code': 'PHP', 'name': 'Philippine Peso', 'country': 'ph', 'region': 'Asia', 'priority': 2},
    {'code': 'VND', 'name': 'Vietnamese Dong', 'country': 'vn', 'region': 'Asia', 'priority': 2},
    {'code': 'TWD', 'name': 'Taiwan Dollar', 'country': 'tw', 'region': 'Asia', 'priority': 2},
    {'code': 'BDT', 'name': 'Bangladeshi Taka', 'country': 'bd', 'region': 'Asia', 'priority': 3},
    {'code': 'PKR', 'name': 'Pakistani Rupee', 'country': 'pk', 'region': 'Asia', 'priority': 3},
    
    # European Currencies
    {'code': 'SEK', 'name': 'Swedish Krona', 'country': 'se', 'region': 'Europe', 'priority': 2},
    {'code': 'NOK', 'name': 'Norwegian Krone', 'country': 'no', 'region': 'Europe', 'priority': 2},
    {'code': 'DKK', 'name': 'Danish Krone', 'country': 'dk', 'region': 'Europe', 'priority': 2},
    {'code': 'PLN', 'name': 'Polish Zloty', 'country': 'pl', 'region': 'Europe', 'priority': 2},
    {'code': 'CZK', 'name': 'Czech Koruna', 'country': 'cz', 'region': 'Europe', 'priority': 2},
    {'code': 'HUF', 'name': 'Hungarian Forint', 'country': 'hu', 'region': 'Europe', 'priority': 2},
    {'code': 'RON', 'name': 'Romanian Leu', 'country': 'ro', 'region': 'Europe', 'priority': 2},
    {'code': 'BGN', 'name': 'Bulgarian Lev', 'country': 'bg', 'region': 'Europe', 'priority': 3},
    {'code': 'HRK', 'name': 'Croatian Kuna', 'country': 'hr', 'region': 'Europe', 'priority': 3},
    {'code': 'ISK', 'name': 'Icelandic Krona', 'country': 'is', 'region': 'Europe', 'priority': 3},
    
    # Americas
    {'code': 'BRL', 'name': 'Brazilian Real', 'country': 'br', 'region': 'South America', 'priority': 2},
    {'code': 'MXN', 'name': 'Mexican Peso', 'country': 'mx', 'region': 'North America', 'priority': 2},
    {'code': 'ARS', 'name': 'Argentine Peso', 'country': 'ar', 'region': 'South America', 'priority': 2},
    {'code': 'CLP', 'name': 'Chilean Peso', 'country': 'cl', 'region': 'South America', 'priority': 2},
    {'code': 'COP', 'name': 'Colombian Peso', 'country': 'co', 'region': 'South America', 'priority': 2},
    {'code': 'PEN', 'name': 'Peruvian Sol', 'country': 'pe', 'region': 'South America', 'priority': 3},
    {'code': 'UYU', 'name': 'Uruguayan Peso', 'country': 'uy', 'region': 'South America', 'priority': 3},
    {'code': 'BOB', 'name': 'Bolivian Boliviano', 'country': 'bo', 'region': 'South America', 'priority': 3},
    {'code': 'PYG', 'name': 'Paraguayan Guarani', 'country': 'py', 'region': 'South America', 'priority': 3},
    {'code': 'VES', 'name': 'Venezuelan Bolívar', 'country': 've', 'region': 'South America', 'priority': 3},
    
    # Middle East & Africa
    {'code': 'SAR', 'name': 'Saudi Riyal', 'country': 'sa', 'region': 'Middle East', 'priority': 2},
    {'code': 'AED', 'name': 'UAE Dirham', 'country': 'ae', 'region': 'Middle East', 'priority': 2},
    {'code': 'ILS', 'name': 'Israeli Shekel', 'country': 'il', 'region': 'Middle East', 'priority': 2},
    {'code': 'TRY', 'name': 'Turkish Lira', 'country': 'tr', 'region': 'Middle East', 'priority': 2},
    {'code': 'EGP', 'name': 'Egyptian Pound', 'country': 'eg', 'region': 'Africa', 'priority': 2},
    {'code': 'ZAR', 'name': 'South African Rand', 'country': 'za', 'region': 'Africa', 'priority': 2},
    {'code': 'NGN', 'name': 'Nigerian Naira', 'country': 'ng', 'region': 'Africa', 'priority': 2},
    {'code': 'KES', 'name': 'Kenyan Shilling', 'country': 'ke', 'region': 'Africa', 'priority': 3},
    {'code': 'GHS', 'name': 'Ghanaian Cedi', 'country': 'gh', 'region': 'Africa', 'priority': 3},
    {'code': 'MAD', 'name': 'Moroccan Dirham', 'country': 'ma', 'region': 'Africa', 'priority': 3},
    
    # Additional Asian Currencies
    {'code': 'RUB', 'name': 'Russian Ruble', 'country': 'ru', 'region': 'Asia/Europe', 'priority': 2},
    {'code': 'KZT', 'name': 'Kazakhstani Tenge', 'country': 'kz', 'region': 'Asia', 'priority': 3},
    {'code': 'UZS', 'name': 'Uzbekistani Som', 'country': 'uz', 'region': 'Asia', 'priority': 3},
    {'code': 'AZN', 'name': 'Azerbaijani Manat', 'country': 'az', 'region': 'Asia', 'priority': 3},
    {'code': 'GEL', 'name': 'Georgian Lari', 'country': 'ge', 'region': 'Asia', 'priority': 3},
    {'code': 'AMD', 'name': 'Armenian Dram', 'country': 'am', 'region': 'Asia', 'priority': 3},
    {'code': 'LKR', 'name': 'Sri Lankan Rupee', 'country': 'lk', 'region': 'Asia', 'priority': 3},
    {'code': 'NPR', 'name': 'Nepalese Rupee', 'country': 'np', 'region': 'Asia', 'priority': 3},
    {'code': 'BTN', 'name': 'Bhutanese Ngultrum', 'country': 'bt', 'region': 'Asia', 'priority': 3},
    {'code': 'MVR', 'name': 'Maldivian Rufiyaa', 'country': 'mv', 'region': 'Asia', 'priority': 3},
    
    # Pacific & Oceania
    {'code': 'NZD', 'name': 'New Zealand Dollar', 'country': 'nz', 'region': 'Oceania', 'priority': 2},
    {'code': 'FJD', 'name': 'Fijian Dollar', 'country': 'fj', 'region': 'Oceania', 'priority': 3},
    {'code': 'PGK', 'name': 'Papua New Guinean Kina', 'country': 'pg', 'region': 'Oceania', 'priority': 3},
    {'code': 'SBD', 'name': 'Solomon Islands Dollar', 'country': 'sb', 'region': 'Oceania', 'priority': 3},
    {'code': 'VUV', 'name': 'Vanuatu Vatu', 'country': 'vu', 'region': 'Oceania', 'priority': 3},
    {'code': 'WST', 'name': 'Samoan Tala', 'country': 'ws', 'region': 'Oceania', 'priority': 3},
    {'code': 'TOP', 'name': 'Tongan Paʻanga', 'country': 'to', 'region': 'Oceania', 'priority': 3},
    
    # Caribbean & Central America
    {'code': 'JMD', 'name': 'Jamaican Dollar', 'country': 'jm', 'region': 'Caribbean', 'priority': 3},
    {'code': 'TTD', 'name': 'Trinidad & Tobago Dollar', 'country': 'tt', 'region': 'Caribbean', 'priority': 3},
    {'code': 'BBD', 'name': 'Barbadian Dollar', 'country': 'bb', 'region': 'Caribbean', 'priority': 3},
    {'code': 'BZD', 'name': 'Belize Dollar', 'country': 'bz', 'region': 'Central America', 'priority': 3},
    {'code': 'GTQ', 'name': 'Guatemalan Quetzal', 'country': 'gt', 'region': 'Central America', 'priority': 3},
    {'code': 'HNL', 'name': 'Honduran Lempira', 'country': 'hn', 'region': 'Central America', 'priority': 3},
    {'code': 'NIO', 'name': 'Nicaraguan Córdoba', 'country': 'ni', 'region': 'Central America', 'priority': 3},
    {'code': 'CRC', 'name': 'Costa Rican Colón', 'country': 'cr', 'region': 'Central America', 'priority': 3},
    {'code': 'PAB', 'name': 'Panamanian Balboa', 'country': 'pa', 'region': 'Central America', 'priority': 3},
    
    # Additional African Currencies
    {'code': 'TND', 'name': 'Tunisian Dinar', 'country': 'tn', 'region': 'Africa', 'priority': 3},
    {'code': 'DZD', 'name': 'Algerian Dinar', 'country': 'dz', 'region': 'Africa', 'priority': 3},
    {'code': 'LYD', 'name': 'Libyan Dinar', 'country': 'ly', 'region': 'Africa', 'priority': 3},
    {'code': 'ETB', 'name': 'Ethiopian Birr', 'country': 'et', 'region': 'Africa', 'priority': 3},
    {'code': 'UGX', 'name': 'Ugandan Shilling', 'country': 'ug', 'region': 'Africa', 'priority': 3},
    {'code': 'TZS', 'name': 'Tanzanian Shilling', 'country': 'tz', 'region': 'Africa', 'priority': 3},
    {'code': 'RWF', 'name': 'Rwandan Franc', 'country': 'rw', 'region': 'Africa', 'priority': 3},
    {'code': 'BIF', 'name': 'Burundian Franc', 'country': 'bi', 'region': 'Africa', 'priority': 3},
    {'code': 'DJF', 'name': 'Djiboutian Franc', 'country': 'dj', 'region': 'Africa', 'priority': 3},
    {'code': 'SOS', 'name': 'Somali Shilling', 'country': 'so', 'region': 'Africa', 'priority': 3},
    
    # Additional Middle Eastern Currencies
    {'code': 'QAR', 'name': 'Qatari Riyal', 'country': 'qa', 'region': 'Middle East', 'priority': 3},
    {'code': 'KWD', 'name': 'Kuwaiti Dinar', 'country': 'kw', 'region': 'Middle East', 'priority': 3},
    {'code': 'BHD', 'name': 'Bahraini Dinar', 'country': 'bh', 'region': 'Middle East', 'priority': 3},
    {'code': 'OMR', 'name': 'Omani Rial', 'country': 'om', 'region': 'Middle East', 'priority': 3},
    {'code': 'JOD', 'name': 'Jordanian Dinar', 'country': 'jo', 'region': 'Middle East', 'priority': 3},
    {'code': 'LBP', 'name': 'Lebanese Pound', 'country': 'lb', 'region': 'Middle East', 'priority': 3},
    {'code': 'SYP', 'name': 'Syrian Pound', 'country': 'sy', 'region': 'Middle East', 'priority': 3},
    {'code': 'IQD', 'name': 'Iraqi Dinar', 'country': 'iq', 'region': 'Middle East', 'priority': 3},
    {'code': 'IRR', 'name': 'Iranian Rial', 'country': 'ir', 'region': 'Middle East', 'priority': 3},
    {'code': 'AFN', 'name': 'Afghan Afghani', 'country': 'af', 'region': 'Middle East', 'priority': 3},
    
    # Additional European Currencies
    {'code': 'UAH', 'name': 'Ukrainian Hryvnia', 'country': 'ua', 'region': 'Europe', 'priority': 3},
    {'code': 'BYN', 'name': 'Belarusian Ruble', 'country': 'by', 'region': 'Europe', 'priority': 3},
    {'code': 'MDL', 'name': 'Moldovan Leu', 'country': 'md', 'region': 'Europe', 'priority': 3},
    {'code': 'RSD', 'name': 'Serbian Dinar', 'country': 'rs', 'region': 'Europe', 'priority': 3},
    {'code': 'BAM', 'name': 'Bosnia-Herzegovina Convertible Mark', 'country': 'ba', 'region': 'Europe', 'priority': 3},
    {'code': 'MKD', 'name': 'Macedonian Denar', 'country': 'mk', 'region': 'Europe', 'priority': 3},
    {'code': 'ALL', 'name': 'Albanian Lek', 'country': 'al', 'region': 'Europe', 'priority': 3},
    {'code': 'EUR', 'name': 'Euro (Montenegro)', 'country': 'me', 'region': 'Europe', 'priority': 3},
    
    # Additional Asian Currencies
    {'code': 'MNT', 'name': 'Mongolian Tugrik', 'country': 'mn', 'region': 'Asia', 'priority': 3},
    {'code': 'KGS', 'name': 'Kyrgyzstani Som', 'country': 'kg', 'region': 'Asia', 'priority': 3},
    {'code': 'TJS', 'name': 'Tajikistani Somoni', 'country': 'tj', 'region': 'Asia', 'priority': 3},
    {'code': 'TMT', 'name': 'Turkmenistani Manat', 'country': 'tm', 'region': 'Asia', 'priority': 3},
    {'code': 'LAK', 'name': 'Laotian Kip', 'country': 'la', 'region': 'Asia', 'priority': 3},
    {'code': 'KHR', 'name': 'Cambodian Riel', 'country': 'kh', 'region': 'Asia', 'priority': 3},
    {'code': 'MMK', 'name': 'Myanmar Kyat', 'country': 'mm', 'region': 'Asia', 'priority': 3},
    {'code': 'BND', 'name': 'Brunei Dollar', 'country': 'bn', 'region': 'Asia', 'priority': 3},
    {'code': 'KPW', 'name': 'North Korean Won', 'country': 'kp', 'region': 'Asia', 'priority': 3},
    {'code': 'MOP', 'name': 'Macanese Pataca', 'country': 'mo', 'region': 'Asia', 'priority': 3},
]

# Enhanced search mappings for intelligent filtering
CURRENCY_SEARCH_MAPPINGS = {
    # Smart abbreviations
    'us': ['USD', 'united states', 'america', 'dollar'],
    'usa': ['USD', 'united states', 'america', 'dollar'],
    'america': ['USD', 'united states', 'dollar'],
    'eu': ['EUR', 'euro', 'europe', 'european union'],
    'europe': ['EUR', 'euro', 'european union'],
    'uk': ['GBP', 'british', 'pound', 'england', 'britain'],
    'britain': ['GBP', 'british', 'pound', 'england'],
    'england': ['GBP', 'british', 'pound', 'britain'],
    'jp': ['JPY', 'japan', 'yen', 'japanese'],
    'japan': ['JPY', 'yen', 'japanese'],
    'cn': ['CNY', 'china', 'yuan', 'chinese'],
    'china': ['CNY', 'yuan', 'chinese'],
    'au': ['AUD', 'australia', 'australian', 'aussie'],
    'australia': ['AUD', 'australian', 'aussie'],
    'ca': ['CAD', 'canada', 'canadian'],
    'canada': ['CAD', 'canadian'],
    'ch': ['CHF', 'switzerland', 'swiss', 'franc'],
    'switzerland': ['CHF', 'swiss', 'franc'],
    'sg': ['SGD', 'singapore'],
    'singapore': ['SGD'],
    'hk': ['HKD', 'hong kong'],
    'in': ['INR', 'india', 'indian', 'rupee'],
    'india': ['INR', 'indian', 'rupee'],
    'kr': ['KRW', 'korea', 'south korea', 'won'],
    'korea': ['KRW', 'south korea', 'won'],
    'id': ['IDR', 'indonesia', 'indonesian', 'rupiah'],
    'indonesia': ['IDR', 'indonesian', 'rupiah'],
    'th': ['THB', 'thailand', 'thai', 'baht'],
    'thailand': ['THB', 'thai', 'baht'],
    'my': ['MYR', 'malaysia', 'malaysian', 'ringgit'],
    'malaysia': ['MYR', 'malaysian', 'ringgit'],
    'ph': ['PHP', 'philippines', 'philippine', 'peso'],
    'philippines': ['PHP', 'philippine', 'peso'],
    'vn': ['VND', 'vietnam', 'vietnamese', 'dong'],
    'vietnam': ['VND', 'vietnamese', 'dong'],
    'br': ['BRL', 'brazil', 'brazilian', 'real'],
    'brazil': ['BRL', 'brazilian', 'real'],
    'mx': ['MXN', 'mexico', 'mexican', 'peso'],
    'mexico': ['MXN', 'mexican', 'peso'],
    'ru': ['RUB', 'russia', 'russian', 'ruble'],
    'russia': ['RUB', 'russian', 'ruble'],
    'tr': ['TRY', 'turkey', 'turkish', 'lira'],
    'turkey': ['TRY', 'turkish', 'lira'],
    'za': ['ZAR', 'south africa', 'south african', 'rand'],
    'sa': ['SAR', 'saudi', 'saudi arabia', 'riyal'],
    'ae': ['AED', 'uae', 'emirates', 'dirham'],
    'il': ['ILS', 'israel', 'israeli', 'shekel'],
    'eg': ['EGP', 'egypt', 'egyptian', 'pound'],
    'ng': ['NGN', 'nigeria', 'nigerian', 'naira'],
}

# Priority-based filtering for performance
def get_currencies_by_priority(priority_level: int = None):
    """Get currencies filtered by priority level for optimized loading"""
    if priority_level is None:
        return SUPPORTED_CURRENCIES
    return [curr for curr in SUPPORTED_CURRENCIES if curr['priority'] <= priority_level]

def get_currencies_by_region(region: str):
    """Get currencies filtered by geographical region"""
    return [curr for curr in SUPPORTED_CURRENCIES if curr['region'].lower() == region.lower()]

def get_top_currencies(limit: int = 20):
    """Get top currencies by priority for fast loading"""
    return sorted(SUPPORTED_CURRENCIES, key=lambda x: x['priority'])[:limit]
