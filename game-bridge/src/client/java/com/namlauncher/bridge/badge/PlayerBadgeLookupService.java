// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.slf4j.Logger;

public final class PlayerBadgeLookupService {
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;
    private static final int CONNECTION_POLL_SECONDS = 1;
    private static final int LOOKUP_INTERVAL_SECONDS = 60;
    private static final long LOOKUP_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(LOOKUP_INTERVAL_SECONDS);
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getGameDir()
        .resolve("config")
        .resolve("namlauncher-player-badge.json");

    private final Logger logger;
    private final ScheduledExecutorService worker;
    private final HttpClient httpClient;
    private final AtomicBoolean inFlight = new AtomicBoolean();
    private final BadgeSnapshotCache snapshotCache = new BadgeSnapshotCache();
    private volatile ClientPacketListener activeConnection;
    private volatile long nextLookupAtNanos;

    public PlayerBadgeLookupService(Logger logger) {
        this.logger = logger;
        this.worker = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "NamLauncher player badge lookup");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((ignored, error) ->
                logger.debug("NamLauncher player badge worker stopped unexpectedly.", error));
            return thread;
        });
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    }

    public void start() {
        worker.scheduleWithFixedDelay(
            this::requestSnapshotOnClientThread,
            0,
            CONNECTION_POLL_SECONDS,
            TimeUnit.SECONDS
        );
    }

    private void requestSnapshotOnClientThread() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            try {
                ClientPacketListener connection = minecraft.getConnection();
                if (connection == null) {
                    if (activeConnection != null) {
                        activeConnection = null;
                        nextLookupAtNanos = 0L;
                        snapshotCache.clear();
                        BadgeRegistry.clear();
                    }
                    return;
                }

                long now = System.nanoTime();
                if (connection != activeConnection) {
                    activeConnection = connection;
                    nextLookupAtNanos = 0L;
                    snapshotCache.clear();
                    BadgeRegistry.clear();
                }

                if (now < nextLookupAtNanos || !inFlight.compareAndSet(false, true)) return;
                nextLookupAtNanos = now + LOOKUP_INTERVAL_NANOS;
                worker.execute(() -> prepareSnapshot(minecraft, connection));
            } catch (RuntimeException error) {
                logger.debug("Could not observe the current Minecraft connection for NamLauncher badges.", error);
            }
        });
    }

    private void prepareSnapshot(Minecraft minecraft, ClientPacketListener connection) {
        PlayerBadgeConfig config = PlayerBadgeConfig.load(CONFIG_PATH);
        if (!config.enabled()) {
            snapshotCache.clear();
            BadgeRegistry.clear();
            inFlight.set(false);
            return;
        }

        minecraft.execute(() -> captureSnapshot(config, minecraft, connection));
    }

    private void captureSnapshot(
        PlayerBadgeConfig config,
        Minecraft minecraft,
        ClientPacketListener connection
    ) {
        UUID selfRuntimeUuid = minecraft.player == null ? null : minecraft.player.getUUID();
        try {
            if (connection != activeConnection || minecraft.getConnection() != connection) {
                inFlight.set(false);
                return;
            }
            BadgeRegistry.replace(selfIdentities(config, selfRuntimeUuid));
            List<BadgeIdentityAliases.VisiblePlayer> visible = connection.getOnlinePlayers().stream()
                .map(PlayerInfo::getProfile)
                .filter(java.util.Objects::nonNull)
                .map(profile -> new BadgeIdentityAliases.VisiblePlayer(profile.id(), profile.name()))
                .toList();
            BadgeIdentityAliases.QueryPlan plan = BadgeIdentityAliases.plan(visible, selfRuntimeUuid);
            worker.execute(() -> lookup(config, plan, selfRuntimeUuid, connection));
        } catch (RuntimeException error) {
            BadgeRegistry.replace(selfIdentities(config, selfRuntimeUuid));
            inFlight.set(false);
            logger.debug("Could not collect the current Tab List for NamLauncher badges.", error);
        }
    }

    private void lookup(
        PlayerBadgeConfig config,
        BadgeIdentityAliases.QueryPlan plan,
        UUID selfRuntimeUuid,
        ClientPacketListener connection
    ) {
        try {
            if (plan.requestedUuids().isEmpty()) {
                Set<UUID> self = selfIdentities(config, selfRuntimeUuid);
                if (isCurrentConnection(connection)) {
                    snapshotCache.record(self, System.nanoTime());
                    BadgeRegistry.replace(self);
                }
                return;
            }
            JsonObject body = new JsonObject();
            JsonArray uuids = new JsonArray();
            plan.requestedUuids().forEach(playerUuid -> uuids.add(playerUuid.toString()));
            body.add("player_uuids", uuids);
            HttpRequest request = HttpRequest.newBuilder(config.lookupEndpoint())
                .timeout(Duration.ofSeconds(config.requestTimeoutSeconds()))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "NamLauncher-Game-Bridge/1.2.4-beta3")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
            HttpResponse<InputStream> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
            );
            if (response.statusCode() != 200) throw new IOException("Badge lookup HTTP " + response.statusCode());
            byte[] bytes;
            try (InputStream input = response.body()) {
                bytes = input.readNBytes(MAX_RESPONSE_BYTES + 1);
            }
            if (bytes.length > MAX_RESPONSE_BYTES) throw new IOException("Badge lookup response is too large");
            Set<UUID> activeCandidates = parseResponse(
                new String(bytes, StandardCharsets.UTF_8), plan.requestedUuids()
            );
            Set<UUID> activeRuntimeUuids = plan.resolve(activeCandidates);
            activeRuntimeUuids.addAll(selfIdentities(config, selfRuntimeUuid));
            if (isCurrentConnection(connection)) {
                snapshotCache.record(activeRuntimeUuids, System.nanoTime());
                BadgeRegistry.replace(activeRuntimeUuids);
            }
        } catch (IOException | InterruptedException | RuntimeException error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            if (isCurrentConnection(connection)) {
                Set<UUID> self = selfIdentities(config, selfRuntimeUuid);
                BadgeRegistry.replace(snapshotCache.fallback(
                    plan.visibleRuntimeUuids(),
                    self,
                    System.nanoTime()
                ));
            }
            logger.debug("NamLauncher player badge lookup is temporarily unavailable.", error);
        } finally {
            inFlight.set(false);
        }
    }

    private boolean isCurrentConnection(ClientPacketListener expected) {
        return expected != null
            && activeConnection == expected
            && Minecraft.getInstance().getConnection() == expected;
    }

    static Set<UUID> parseResponse(String raw, Collection<UUID> requested) {
        Set<UUID> allowed = Set.copyOf(requested);
        JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
        if (!root.get("ok").getAsBoolean()) throw new IllegalArgumentException("Badge lookup failed");
        JsonArray values = root.getAsJsonArray("active_player_uuids");
        if (values == null || values.size() > BadgeIdentityAliases.MAX_QUERY_UUIDS) {
            throw new IllegalArgumentException("Invalid badge batch");
        }
        HashSet<UUID> active = new HashSet<>();
        for (JsonElement value : values) {
            UUID playerUuid = UUID.fromString(value.getAsString().toLowerCase());
            if (!allowed.contains(playerUuid) || !active.add(playerUuid)) {
                throw new IllegalArgumentException("Badge response contains an unexpected UUID");
            }
        }
        return active;
    }

    private static Set<UUID> selfIdentities(PlayerBadgeConfig config, UUID selfRuntimeUuid) {
        LinkedHashSet<UUID> self = new LinkedHashSet<>();
        if (selfRuntimeUuid != null) self.add(selfRuntimeUuid);
        if (config.selfPlayerUuid() != null) self.add(config.selfPlayerUuid());
        return self;
    }
}
