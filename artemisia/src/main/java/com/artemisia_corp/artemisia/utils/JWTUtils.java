package com.artemisia_corp.artemisia.utils;

import lombok.NonNull;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.regex.Pattern;

public class JWTUtils {
    private JWTUtils(){}
    public static boolean isTokenExpired(@NonNull String jwt, String fieldNameExp, @NonNull Long ventana)  {
        String aux = jwt.split(Pattern.quote("."))[1];

        JSONObject element = new JSONObject(new String(Base64.getDecoder().decode(aux)));
        long timeExpire = element.getLong(StringUtils.isEmpty(fieldNameExp) ? "exp" : fieldNameExp);
        return System.currentTimeMillis() >= (timeExpire * 1000) + ventana;
    }

    public static JSONObject getPayload(@NonNull String jwt)  {
        String aux = jwt.split(Pattern.quote("."))[1];
        return new JSONObject(new String(Base64.getDecoder().decode(aux)));
    }
}
