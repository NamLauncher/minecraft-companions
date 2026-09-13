// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Maps server/proxy UUIDs to the privacy-preserving aliases already published by the launcher. */
public final class BadgeIdentityAliases {
    public static final int MAX_VISIBLE_PLAYERS = 512;
    public static final int MAX_QUERY_UUIDS = MAX_VISIBLE_PLAYERS * 2;

    private BadgeIdentityAliases() { }

    public record VisiblePlayer(UUID runtimeUuid, String playerName) { }

    public record QueryPlan(
        List<UUID> requestedUuids,
        Map<UUID, UUID> candidateToRuntimeUuid,
        Set<UUID> visibleRuntimeUuids
    ) {
        public Set<UUID> resolve(Collection<UUID> activeCandidates) {
            LinkedHashSet<UUID> resolved = new LinkedHashSet<>();
            if (activeCandidates == null) return resolved;
            for (UUID candidate : activeCandidates) {
                UUID runtime = candidateToRuntimeUuid.get(candidate);
                if (runtime != null) resolved.add(runtime);
            }
            return resolved;
        }
    }

    public static QueryPlan plan(Collection<VisiblePlayer> visiblePlayers, UUID selfRuntimeUuid) {
        ArrayList<VisiblePlayer> ordered = new ArrayList<>();
        if (visiblePlayers != null) {
            visiblePlayers.stream()
                .filter(player -> player != null && player.runtimeUuid() != null)
                .sorted(Comparator.comparing(player -> player.runtimeUuid().toString()))
                .limit(MAX_VISIBLE_PLAYERS)
                .forEach(ordered::add);
        }
        if (selfRuntimeUuid != null) {
            ordered.removeIf(player -> selfRuntimeUuid.equals(player.runtimeUuid()));
            ordered.add(0, new VisiblePlayer(selfRuntimeUuid, null));
            if (ordered.size() > MAX_VISIBLE_PLAYERS) ordered.remove(ordered.size() - 1);
        }

        LinkedHashMap<UUID, UUID> aliases = new LinkedHashMap<>();
        LinkedHashSet<UUID> runtimes = new LinkedHashSet<>();
        for (VisiblePlayer player : ordered) {
            UUID runtime = player.runtimeUuid();
            if (!runtimes.add(runtime)) continue;
            aliases.putIfAbsent(runtime, runtime);
            UUID offline = offlinePlayerUuid(player.playerName());
            if (offline != null) aliases.putIfAbsent(offline, runtime);
        }
        return new QueryPlan(List.copyOf(aliases.keySet()), Map.copyOf(aliases), Set.copyOf(runtimes));
    }

    public static UUID offlinePlayerUuid(String playerName) {
        if (playerName == null || !playerName.matches("[A-Za-z0-9_]{1,16}")) return null;
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }
}
