package com.artemisia_corp.artemisia.utils;

import lombok.NonNull;
//import org.apache.commons.lang3.StringUtils;
//import org.json.JSONObject;

import java.util.Base64;
import java.util.regex.Pattern;

public class JWTUtils {
//    private JWTUtils() {
//    }
//
//    public static boolean isTokenExpired(@NonNull String jwt, String fieldNameExp, @NonNull Long ventana) {
//        String aux = jwt.split(Pattern.quote("."))[1];
//
//        JSONObject element = new JSONObject(new String(Base64.getDecoder().decode(aux)));
//        long timeExpire = element.getLong(StringUtils.isEmpty(fieldNameExp) ? "exp" : fieldNameExp);
//        return System.currentTimeMillis() >= (timeExpire * 1000) + ventana;
//    }
//
//    public static JSONObject getPayload(@NonNull String jwt) {
//        String aux = jwt.split(Pattern.quote("."))[1];
//        return new JSONObject(new String(Base64.getDecoder().decode(aux)));
//    }
//
//    public static void main(String[] args) {
//        System.out.println(JWTUtils.isTokenExpired("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0MjRibXNjIiwianRpIjoiNGM5YTQ5NzItYTljZS00NTkzLTlmMmItMGQxZTJhMzJhNDllIiwiaWF0IjoxNzUwMTgwNjMxLCJleHAiOjE3NTAyMDk0MzEsImF1ZCI6InN0ZXJldW0iLCJ1c2VyX2lkIjoiNGM5YTQ5NzItYTljZS00NTkzLTlmMmItMGQxZTJhMzJhNDllIiwiZW1haWxfdmVyaWZpZWQiOnRydWV9.b3fLn0ofkpyHmtVnLjAZLhXgoS6MV3ZhHWKNhkVWIoI", null, 1L));
//    }
}
