package com.artemisia_corp.artemisia.utils;

import java.util.regex.Pattern;

public class SQLInjectionChecker {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(\\b(select|update|delete|insert|exec|drop|alter|grant|merge|sp_executesql|union|create)\\b|" + // SQL keywords
                    "--|'|\"|;|\\*|--\\s+|\\/\\*|\\*\\/|xp_|0x)", // Special characters and commands
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isSafe(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        return !SQL_INJECTION_PATTERN.matcher(input).find();
    }

    public static void validate(String input) {
        if (!isSafe(input)) {
            throw new IllegalArgumentException("Input string contains potential SQL injection patterns.");
        }
    }
}