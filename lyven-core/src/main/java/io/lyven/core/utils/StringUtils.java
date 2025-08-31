package io.lyven.core.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * String utilities for Lyven framework
 * Provides common string manipulation operations
 */
public class StringUtils {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([a-z])([A-Z])");
    private static final Pattern KEBAB_CASE_PATTERN = Pattern.compile("-([a-z])");
    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("_([a-z])");

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Check if string is null, empty, or contains only whitespace
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Capitalize first letter of string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Uncapitalize first letter of string
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Convert camelCase to kebab-case
     * Example: "userName" -> "user-name"
     */
    public static String camelToKebab(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return CAMEL_CASE_PATTERN.matcher(str).replaceAll("$1-$2").toLowerCase();
    }

    /**
     * Convert kebab-case to camelCase
     * Example: "user-name" -> "userName"
     */
    public static String kebabToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return KEBAB_CASE_PATTERN.matcher(str).replaceAll(match -> match.group(1).toUpperCase());
    }

    /**
     * Convert camelCase to snake_case
     * Example: "userName" -> "user_name"
     */
    public static String camelToSnake(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return CAMEL_CASE_PATTERN.matcher(str).replaceAll("$1_$2").toLowerCase();
    }

    /**
     * Convert snake_case to camelCase
     * Example: "user_name" -> "userName"
     */
    public static String snakeToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return SNAKE_CASE_PATTERN.matcher(str).replaceAll(match -> match.group(1).toUpperCase());
    }

    /**
     * Convert string to PascalCase
     * Example: "user name" -> "UserName"
     */
    public static String toPascalCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return Arrays.stream(str.split("[\\s_-]+"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }

    /**
     * Convert string to camelCase
     * Example: "user name" -> "userName"
     */
    public static String toCamelCase(String str) {
        String pascal = toPascalCase(str);
        return uncapitalize(pascal);
    }

    /**
     * Join array of strings with delimiter
     */
    public static String join(String delimiter, String... elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }
        return String.join(delimiter, elements);
    }

    /**
     * Join collection of strings with delimiter
     */
    public static String join(String delimiter, Collection<String> elements) {
        if (elements == null || elements.isEmpty()) {
            return "";
        }
        return String.join(delimiter, elements);
    }

    /**
     * Split string and trim each element
     */
    public static List<String> splitAndTrim(String str, String delimiter) {
        if (isEmpty(str)) {
            return Collections.emptyList();
        }

        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    /**
     * Pad string to left with specified character
     */
    public static String padLeft(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }

        if (str.length() >= length) {
            return str;
        }

        return String.valueOf(padChar).repeat(length - str.length()) +
                str;
    }

    /**
     * Pad string to right with specified character
     */
    public static String padRight(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }

        if (str.length() >= length) {
            return str;
        }

        return str + String.valueOf(padChar).repeat(length - str.length());
    }

    /**
     * Remove prefix from string if present
     */
    public static String removePrefix(String str, String prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return str;
        }

        if (str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }

        return str;
    }

    /**
     * Remove suffix from string if present
     */
    public static String removeSuffix(String str, String suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return str;
        }

        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }

        return str;
    }

    /**
     * Truncate string to maximum length
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength);
    }

    /**
     * Truncate string with ellipsis
     */
    public static String truncateWithEllipsis(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        if (maxLength <= 3) {
            return truncate(str, maxLength);
        }

        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Count occurrences of substring in string
     */
    public static int countOccurrences(String str, String substring) {
        if (isEmpty(str) || isEmpty(substring)) {
            return 0;
        }

        int count = 0;
        int index = 0;

        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }

        return count;
    }

    /**
     * Reverse string
     */
    public static String reverse(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return new StringBuilder(str).reverse().toString();
    }

    /**
     * Check if string contains only digits
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }

        return str.chars().allMatch(Character::isDigit);
    }

    /**
     * Check if string contains only letters
     */
    public static boolean isAlpha(String str) {
        if (isEmpty(str)) {
            return false;
        }

        return str.chars().allMatch(Character::isLetter);
    }

    /**
     * Check if string contains only letters and digits
     */
    public static boolean isAlphanumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }

        return str.chars().allMatch(Character::isLetterOrDigit);
    }

    /**
     * Generate random string of specified length
     */
    public static String randomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }

    /**
     * Get default string if input is null or empty
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Get default string if input is null or blank
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * Repeat string n times
     */
    public static String repeat(String str, int times) {
        if (isEmpty(str) || times <= 0) {
            return "";
        }

        return str.repeat(times);
    }
}