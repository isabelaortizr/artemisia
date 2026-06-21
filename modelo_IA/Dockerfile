FROM python:3.10-slim

WORKDIR /app

RUN apt-get update && apt-get install -y libpq-dev gcc && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiamos todo el contenido como el paquete modelo_IA
COPY . /app/modelo_IA/

# Asegurar que sea un paquete Python válido
RUN touch /app/modelo_IA/__init__.py

# Directorios que necesita la app en runtime
RUN mkdir -p /app/modelo_IA/models/trained /app/modelo_IA/data_exports

EXPOSE 7860

CMD ["uvicorn", "modelo_IA.tools.recommender_api:APP", "--host", "0.0.0.0", "--port", "7860"]
