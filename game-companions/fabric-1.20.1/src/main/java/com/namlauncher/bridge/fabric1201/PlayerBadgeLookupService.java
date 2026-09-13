// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

final class PlayerBadgeLookupService {
    private final Logger logger;
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "NamLauncher badge lookup");
        thread.setDaemon(true);
        return thread;
    });
    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(4)).followRedirects(HttpClient.Redirect.NEVER).build();
    private final AtomicBoolean inFlight = new AtomicBoolean();
    private final Path configPath = FabricLoader.getInstance().getGameDir()
        .resolve("config").resolve("namlauncher-player-badge.json");

    PlayerBadgeLookupService(Logger logger) {
        this.logger = logger;
    }

    void start() {
        worker.scheduleWithFixedDelay(this::capture, 2, 15, TimeUnit.SECONDS);
    }

    private void capture() {
        if (!inFlight.compareAndSet(false, true)) return;
        PlayerBadgeConfig config = PlayerBadgeConfig.load(configPath);
        if (!config.enabled()) {
            BadgeRegistry.clear();
            inFlight.set(false);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            try {
                if (minecraft.getConnection() == null) {
                    BadgeRegistry.replace(Set.of(config.selfUuid()));
                    inFlight.set(false);
                    return;
                }
                List<UUID> players = minecraft.getConnection().getOnlinePlayers().stream()
                    .map(info -> info.getProfile().getId()).filter(java.util.Objects::nonNull)
                    .distinct().sorted().limit(100).toList();
                worker.execute(() -> lookup(config, players));
            } catch (RuntimeException error) {
                BadgeRegistry.replace(Set.of(config.selfUuid()));
                inFlight.set(false);
            }
        });
    }

    private void lookup(PlayerBadgeConfig config, List<UUID> requested) {
        try {
            JsonArray values = new JsonArray();
            requested.forEach(uuid -> values.add(uuid.toString()));
            JsonObject body = new JsonObject();
            body.add("player_uuids", values);
            HttpRequest request = HttpRequest.newBuilder(config.endpoint())
                .timeout(Duration.ofSeconds(config.timeoutSeconds()))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "NamLauncher-Game-Companion/1.1.16")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8)).build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) throw new IllegalStateException("HTTP " + response.statusCode());
            byte[] bytes;
            try (InputStream input = response.body()) {
                bytes = input.readNBytes(64 * 1024 + 1);
            }
            if (bytes.length > 64 * 1024) throw new IllegalStateException("Response too large");
            JsonArray activeValues = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonArray("active_player_uuids");
            if (activeValues.size() > 100) throw new IllegalStateException("Invalid batch");
            Set<UUID> allowed = Set.copyOf(requested);
            HashSet<UUID> active = new HashSet<>();
            activeValues.forEach(value -> {
                UUID uuid = UUID.fromString(value.getAsString().toLowerCase());
                if (!allowed.contains(uuid) || !active.add(uuid)) throw new IllegalArgumentException("Unexpected UUID");
            });
            active.add(config.selfUuid());
            BadgeRegistry.replace(active);
        } catch (Exception error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            BadgeRegistry.replace(Set.of(config.selfUuid()));
            logger.debug("NamLauncher player badges are temporarily unavailable.", error);
        } finally {
            inFlight.set(false);
        }
    }
}
