// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class BadgeRegistry {
    private static final AtomicReference<Set<UUID>> ACTIVE = new AtomicReference<>(Set.of());

    private BadgeRegistry() {
    }

    public static boolean has(UUID playerUuid) {
        return playerUuid != null && ACTIVE.get().contains(playerUuid);
    }

    public static void replace(Collection<UUID> playerUuids) {
        HashSet<UUID> bounded = new HashSet<>();
        if (playerUuids != null) {
            for (UUID playerUuid : playerUuids) {
                if (playerUuid != null) bounded.add(playerUuid);
                if (bounded.size() >= 100) break;
            }
        }
        ACTIVE.set(Set.copyOf(bounded));
    }

    public static void clear() {
        ACTIVE.set(Set.of());
    }
}
