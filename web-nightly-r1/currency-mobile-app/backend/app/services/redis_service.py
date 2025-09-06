import json
import redis.asyncio as redis
from typing import Optional, Any
from app.core.config import settings

class RedisService:
    _client: Optional[redis.Redis] = None
    
    @classmethod
    def set_client(cls, client: redis.Redis):
        cls._client = client
    
    @classmethod
    async def get(cls, key: str) -> Optional[str]:
        if not cls._client:
            return None
        try:
            return await cls._client.get(key)
        except Exception:
            return None
    
    @classmethod
    async def set(cls, key: str, value: Any, ttl: int = settings.CACHE_TTL):
        if not cls._client:
            return False
        try:
            if isinstance(value, (dict, list)):
                value = json.dumps(value)
            await cls._client.setex(key, ttl, value)
            return True
        except Exception:
            return False
    
    @classmethod
    async def get_json(cls, key: str) -> Optional[dict]:
        data = await cls.get(key)
        if data:
            try:
                return json.loads(data)
            except json.JSONDecodeError:
                return None
        return None
    
    @classmethod
    async def delete(cls, key: str) -> bool:
        if not cls._client:
            return False
        try:
            await cls._client.delete(key)
            return True
        except Exception:
            return False
    
    @classmethod
    async def exists(cls, key: str) -> bool:
        if not cls._client:
            return False
        try:
            return bool(await cls._client.exists(key))
        except Exception:
            return False
