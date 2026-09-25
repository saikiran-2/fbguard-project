package com.fbguard.backend.service.verification;

import java.net.URI;
import java.util.regex.Pattern;

final class UrlUtils {

    private static final Pattern IP_PATTERN =
            Pattern.compile("^(https?://)?\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

    private static final Pattern SHORTENER_PATTERN = Pattern.compile(
            "(bit\\.ly|tinyurl\\.com|t\\.co|goo\\.gl|is\\.gd|ow\\.ly|buff\\.ly|rebrand\\.ly)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SUSPICIOUS_TLD =
            Pattern.compile("\\.(tk|ml|ga|cf|gq|xyz|top|click|work)$", Pattern.CASE_INSENSITIVE);

    private UrlUtils() {}

    static String extractHost(String url) {
        try {
            URI uri = URI.create(url.trim());
            return uri.getHost();
        } catch (Exception e) {
            return null;
        }
    }

    static boolean looksLikeIp(String url) {
        return IP_PATTERN.matcher(url).find();
    }

    static boolean isShortener(String url) {
        return SHORTENER_PATTERN.matcher(url).find();
    }

    static boolean hasSuspiciousTld(String host) {
        return host != null && SUSPICIOUS_TLD.matcher(host).find();
    }

    static boolean isHttps(String url) {
        return url.trim().toLowerCase().startsWith("https://");
    }

    static boolean isOfficialFacebookHost(String host) {
        if (host == null) return false;
        String h = host.toLowerCase();
        return h.equals("facebook.com") || h.endsWith(".facebook.com")
                || h.equals("apps.facebook.com") || h.equals("fb.gg") || h.endsWith(".fb.gg");
    }
}
