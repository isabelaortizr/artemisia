package com.artemisia_corp.artemisia.service.impl.clients;

import com.artemisia_corp.artemisia.service.LogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Component
@Slf4j
public class RecommenderPythonClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${recommender_python_url}")
    private String recommenderUrl;

    @Autowired(required = false)
    private LogsService logsService;

    @Value("${recommender_python_api_key:}")
    private String recommenderApiKey;

    public Map[] getRecommendations(int userId, int topN) {
        String url = String.format("%s/recommendations/%d?limit=%d", recommenderUrl, userId, topN);
        ResponseEntity<Map[]> resp = restTemplate.getForEntity(url, Map[].class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }
        return new Map[0];
    }

    public Map getSimilarUsers(int userId, int limit) {
        String url = String.format("%s/similar_users/%d?limit=%d", recommenderUrl, userId, limit);
        ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }
        return Map.of();
    }

    /**
     * Helper that returns the similar users as an array of Longs by parsing the map returned by the API.
     */
    @SuppressWarnings("unchecked")
    public Long[] getSimilarUsersAsArray(int userId, int limit) {
        Map<String, Object> resp = getSimilarUsers(userId, limit);
        if (resp == null || !resp.containsKey("similar_users")) {
            return new Long[0];
        }

        Object obj = resp.get("similar_users");
        if (!(obj instanceof List)) {
            return new Long[0];
        }

        List<Object> list = (List<Object>) obj;
        List<Long> longs = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Number) longs.add(((Number) o).longValue());
            else {
                try { longs.add(Long.parseLong(o.toString())); }
                catch (Exception e) {
                    logsService.error(e.getMessage());
                }
            }
        }

        return longs.toArray(new Long[0]);
    }

    /**
     * Send training payload to the Python service (/train). Returns the server response string (if any).
     */
    public String train(Object trainingPayload) {
        String url = String.format("%s/train_trigger?source=db", recommenderUrl);
        ResponseEntity<String> resp = restTemplate.postForEntity(url, trainingPayload, String.class);
        return resp.getBody();
    }

    /**
     * Notify python service about a product view event.
     */
    public boolean notifyView(int userId, int productId, Integer durationSeconds) {
        try {
            String url = String.format("%s/update-view", recommenderUrl);
            // The Python service expects api_key inside the JSON payload (if configured).
            // Build a mutable payload so we can conditionally add the api_key.
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("user_id", userId);
            payload.put("product_id", productId);
            payload.put("duration", durationSeconds);
            if (recommenderApiKey != null && !recommenderApiKey.isBlank()) {
                payload.put("api_key", recommenderApiKey);
            }
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(payload);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, entity, Map.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            if (logsService != null) logsService.error("notifyView error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Notify python service about a purchase event (multiple products).
     */
    public boolean notifyPurchase(int userId, List<Integer> productIds, Double eventWeight) {
        try {
            String url = String.format("%s/update-purchase", recommenderUrl);
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("user_id", userId);
            payload.put("product_ids", productIds);
            payload.put("event_weight", eventWeight);
            if (recommenderApiKey != null && !recommenderApiKey.isBlank()) {
                payload.put("api_key", recommenderApiKey);
            }
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(payload);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, entity, Map.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            if (logsService != null) logsService.error("notifyPurchase error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns recommendation product ids as Integer array (calls python /recommendations endpoint).
     */
    public Integer[] getRecommendationIds(int userId, int topN) {
        try {
            String url = String.format("%s/recommendations/%d?limit=%d", recommenderUrl, userId, topN);
            ResponseEntity<Integer[]> resp = restTemplate.getForEntity(url, Integer[].class);
            if (resp.getStatusCode() == HttpStatus.OK) return resp.getBody();
        } catch (Exception e) {
            if (logsService != null) logsService.error("getRecommendationIds error: " + e.getMessage());
        }
        return new Integer[0];
    }

    /**
     * Register a newly created user in the recommender (create empty preference entry).
     */
    public boolean registerUser(int userId) {
        try {
            String url = String.format("%s/register_user", recommenderUrl);
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("user_id", userId);
            if (recommenderApiKey != null && !recommenderApiKey.isBlank()) {
                payload.put("api_key", recommenderApiKey);
            }
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(payload);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, entity, Map.class);
            log.info(resp.getBody().toString());
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            if (logsService != null) logsService.error("registerUser error: " + e.getMessage());
            return false;
        }
    }
}
