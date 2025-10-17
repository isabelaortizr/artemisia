# Artemisia Recommendation Service (Python microservice)

This microservice implements a recommendation engine and endpoints to update user preference vectors from product views and purchases.

Quick start (dev):

1. Create and activate a venv (PowerShell):

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

2. Create DB columns (run the SQL in `deploy/DATABASE_CHANGES.sql`) against your Postgres DB.

3. Run the service locally:

```powershell
$env:FLASK_APP = 'app.py'
python -m flask run --host=0.0.0.0 --port=5000
```

Endpoints:
- POST /update-view  { user_id: int, product_id: int, duration?: int }
- POST /update-purchase { user_id: int, product_ids: [int] }
- GET /recommendations/<user_id>
- POST /train (starts async training)

Integration with Java (example):
- After persisting a `ProductView` in Java, call `POST http://{python_service}:5000/update-view` with JSON payload. Use an async HTTP client (RestTemplate/WebClient) so not to block request thread.
- After completing a `NotaVenta` (PAYED), build list of product IDs and POST to `/update-purchase`.

Java example (Spring RestTemplate, async):

```java
// Using async rest template or WebClient is recommended; simple example with RestTemplate:
RestTemplate rest = new RestTemplate();
String url = "http://python-service:5000/update-view";
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
headers.set("X-API-Key", "your-internal-key");

Map<String,Object> body = Map.of("user_id", userId, "product_id", productId, "duration", durationInSeconds);
HttpEntity<Map<String,Object>> request = new HttpEntity<>(body, headers);
rest.postForEntity(url, request, Void.class);
```

Docker run example:

```powershell
docker build -t artemisia-recommender:latest .
docker run -e DB_HOST=... -e DB_USER=... -e DB_PASSWORD=... -e RECOMMENDER_API_KEY=your-internal-key -p 5000:5000 artemisia-recommender:latest
```

Notes:
- By default the microservice expects the DB configured in `config/settings.py` and uses the `config.database.db` connection.
- Consider protecting internal endpoints with an API key or running service in private network.
