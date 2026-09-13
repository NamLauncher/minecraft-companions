// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class BadgeRegistry {
    private static final int MAX_ACTIVE_PLAYERS = BadgeIdentityAliases.MAX_VISIBLE_PLAYERS;
    private static final AtomicReference<Set<UUID>> ACTIVE = new AtomicReference<>(Set.of());

    private BadgeRegistry() {
    }

    public static boolean hasBadge(UUID playerUuid) {
        return playerUuid != null && ACTIVE.get().contains(playerUuid);
    }

    public static void replace(Collection<UUID> playerUuids) {
        if (playerUuids == null || playerUuids.isEmpty()) {
            clear();
            return;
        }
        HashSet<UUID> bounded = new HashSet<>();
        for (UUID playerUuid : playerUuids) {
            if (playerUuid == null) continue;
            bounded.add(playerUuid);
            if (bounded.size() >= MAX_ACTIVE_PLAYERS) break;
        }
        Set<UUID> next = Set.copyOf(bounded);
        ACTIVE.updateAndGet(current -> current.equals(next) ? current : next);
    }

    public static void clear() {
        ACTIVE.updateAndGet(current -> current.isEmpty() ? current : Set.of());
    }

    public static int size() {
        return ACTIVE.get().size();
    }

    public static Set<UUID> snapshot() {
        return ACTIVE.get();
    }
}
