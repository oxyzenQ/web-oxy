# Kconvert - Optimized Production Dockerfile
FROM python:3.11-slim

WORKDIR /app

# Environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONPATH=/app

# Install dependencies in one layer
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy application
COPY main_optimized.py .
COPY .env* ./

# Security: non-root user
RUN useradd --create-home --shell /bin/bash app && \
    chown -R app:app /app
USER app

EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=2 \
    CMD python -c "import httpx; httpx.get('http://localhost:8000/', timeout=5)"

# Run optimized app
CMD ["uvicorn", "production_start:app", "--host", "0.0.0.0", "--port", "8000"]
