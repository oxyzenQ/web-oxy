from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    # Redis Configuration
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    
    # External API Configuration
    EXCHANGE_API_KEY: str = "de1695208ebf652f2f84fe41"  # From original project
    EXCHANGE_API_URL: str = "https://v6.exchangerate-api.com/v6"
    
    # Fallback APIs
    FIXER_API_KEY: Optional[str] = None
    FIXER_API_URL: str = "https://api.fixer.io/v1"
    
    # Cache Configuration
    CACHE_TTL: int = 3600  # 1 hour in seconds
    RATE_LIMIT_PER_MINUTE: int = 100
    
    # App Configuration
    DEBUG: bool = True
    API_V1_STR: str = "/api/v1"
    PROJECT_NAME: str = "Currency Converter API"
    
    class Config:
        env_file = ".env"

settings = Settings()
