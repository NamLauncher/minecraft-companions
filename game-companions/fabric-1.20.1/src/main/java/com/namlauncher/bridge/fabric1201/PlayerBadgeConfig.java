// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.UUID;

record PlayerBadgeConfig(boolean enabled, UUID selfUuid, URI endpoint, int timeoutSeconds) {
    static PlayerBadgeConfig disabled() {
        return new PlayerBadgeConfig(false, null, null, 4);
    }

    static PlayerBadgeConfig load(Path path) {
        try {
            if (Files.isSymbolicLink(path) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                || Files.size(path) <= 0 || Files.size(path) > 16 * 1024) return disabled();
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.get("schemaVersion").getAsInt() != 1 || !root.get("enabled").getAsBoolean()) return disabled();
            UUID self = UUID.fromString(root.get("selfPlayerUuid").getAsString().toLowerCase());
            URI endpoint = URI.create(root.get("lookupEndpoint").getAsString());
            if (!allowed(endpoint)) return disabled();
            int timeout = Math.max(2, Math.min(8, root.get("requestTimeoutSeconds").getAsInt()));
            return new PlayerBadgeConfig(true, self, endpoint, timeout);
        } catch (Exception ignored) {
            return disabled();
        }
    }

    private static boolean allowed(URI endpoint) {
        if (endpoint == null || endpoint.getUserInfo() != null || endpoint.getQuery() != null
            || endpoint.getFragment() != null || !"/api/player-badges/lookup".equals(endpoint.getPath())) return false;
        String host = endpoint.getHost() == null ? "" : endpoint.getHost().toLowerCase();
        if ("https".equalsIgnoreCase(endpoint.getScheme()) && "namlauncher.nattapat2871.me".equals(host)) {
            return endpoint.getPort() == -1 || endpoint.getPort() == 443;
        }
        return "http".equalsIgnoreCase(endpoint.getScheme())
            && ("127.0.0.1".equals(host) || "localhost".equals(host));
    }
}
