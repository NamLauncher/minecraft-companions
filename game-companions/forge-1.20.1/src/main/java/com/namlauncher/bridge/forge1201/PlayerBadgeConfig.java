// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

record PlayerBadgeConfig(boolean enabled, UUID selfUuid, URI endpoint, int timeoutSeconds) {
    private static final URI DEFAULT_ENDPOINT = URI.create("https://namlauncher.nattapat2871.me/api/player-badges/lookup");
    static PlayerBadgeConfig load(Path path) {
        try {
            if (!Files.isRegularFile(path) || Files.size(path) > 16 * 1024) return disabled();
            JsonObject value = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            boolean enabled = value.has("enabled") && value.get("enabled").getAsBoolean();
            UUID selfUuid = UUID.fromString(value.get("selfUuid").getAsString().toLowerCase());
            URI endpoint = URI.create(value.get("lookupEndpoint").getAsString());
            if (!isAllowedEndpoint(endpoint)) return disabled();
            int timeout = value.has("timeoutSeconds") ? value.get("timeoutSeconds").getAsInt() : 4;
            return new PlayerBadgeConfig(enabled, selfUuid, endpoint, Math.max(2, Math.min(8, timeout)));
        } catch (Exception ignored) { return disabled(); }
    }
    private static boolean isAllowedEndpoint(URI uri) {
        if (DEFAULT_ENDPOINT.equals(uri)) return true;
        String host = uri.getHost();
        return "http".equalsIgnoreCase(uri.getScheme()) && ("127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host));
    }
    private static PlayerBadgeConfig disabled() { return new PlayerBadgeConfig(false, new UUID(0, 0), DEFAULT_ENDPOINT, 4); }
}
