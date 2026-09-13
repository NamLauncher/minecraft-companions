// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class BadgeRegistry {
    private static final AtomicReference<Set<UUID>> ACTIVE = new AtomicReference<>(Set.of());
    private BadgeRegistry() { }
    public static boolean isActive(UUID uuid) { return uuid != null && ACTIVE.get().contains(uuid); }
    static void replace(Set<UUID> values) { ACTIVE.set(Set.copyOf(values)); }
    static void clear() { ACTIVE.set(Set.of()); }
}
