package com.artemisia_corp.artemisia.service.impl.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import java.util.Map;

@Component
public class RecommenderPythonClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${recommender.python.url:http://localhost:8000}")
    private String recommenderUrl;

    public Map[] getRecommendations(int userId, int topN) {
        String url = String.format("%s/recommendations/%d?top_n=%d", recommenderUrl, userId, topN);
        ResponseEntity<Map[]> resp = restTemplate.getForEntity(url, Map[].class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }
        return new Map[0];
    }

    public Map<String, Object> getSimilarUsers(int userId, int limit) {
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
                try { longs.add(Long.parseLong(o.toString())); } catch (Exception ignored) {}
            }
        }

        return longs.toArray(new Long[0]);
    }

    /**
     * Send training payload to the Python service (/train). Returns the server response string (if any).
     */
    public String train(Object trainingPayload) {
        String url = String.format("%s/train", recommenderUrl);
        ResponseEntity<String> resp = restTemplate.postForEntity(url, trainingPayload, String.class);
        return resp.getBody();
    }
}
