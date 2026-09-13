// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.UUID;

public record PlayerBadgeConfig(
    boolean enabled,
    UUID selfPlayerUuid,
    URI lookupEndpoint,
    int refreshSeconds,
    int requestTimeoutSeconds
) {
    private static final int MAX_CONFIG_BYTES = 16 * 1024;
    private static final String EXPECTED_PATH = "/api/player-badges/lookup";

    public static PlayerBadgeConfig disabled() {
        return new PlayerBadgeConfig(false, null, null, 30, 4);
    }

    public static PlayerBadgeConfig load(Path path) {
        try {
            if (path == null || Files.isSymbolicLink(path) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                return disabled();
            }
            long size = Files.size(path);
            if (size <= 0 || size > MAX_CONFIG_BYTES) return disabled();
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            if (root.get("schemaVersion").getAsInt() != 1 || !root.get("enabled").getAsBoolean()) {
                return disabled();
            }
            UUID self = UUID.fromString(root.get("selfPlayerUuid").getAsString().toLowerCase());
            URI endpoint = URI.create(root.get("lookupEndpoint").getAsString());
            if (!isAllowedEndpoint(endpoint)) return disabled();
            int refresh = Math.clamp(root.get("refreshSeconds").getAsInt(), 15, 60);
            int timeout = Math.clamp(root.get("requestTimeoutSeconds").getAsInt(), 2, 8);
            return new PlayerBadgeConfig(true, self, endpoint, refresh, timeout);
        } catch (IOException | RuntimeException ignored) {
            return disabled();
        }
    }

    static boolean isAllowedEndpoint(URI endpoint) {
        if (endpoint == null || endpoint.getUserInfo() != null || endpoint.getQuery() != null
            || endpoint.getFragment() != null || !EXPECTED_PATH.equals(endpoint.getPath())) {
            return false;
        }
        String host = endpoint.getHost() == null ? "" : endpoint.getHost().toLowerCase();
        if ("https".equalsIgnoreCase(endpoint.getScheme())
            && "namlauncher.nattapat2871.me".equals(host)) {
            return endpoint.getPort() == -1 || endpoint.getPort() == 443;
        }
        return "http".equalsIgnoreCase(endpoint.getScheme())
            && ("127.0.0.1".equals(host) || "localhost".equals(host));
    }
}
