// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Keeps one short-lived, server-scoped successful lookup across transient API failures. */
final class BadgeSnapshotCache {
    static final long MAX_AGE_NANOS = Duration.ofSeconds(45).toNanos();

    private final long maxAgeNanos;
    private Set<UUID> lastKnownGood = Set.of();
    private long recordedAtNanos;
    private boolean populated;

    BadgeSnapshotCache() {
        this(MAX_AGE_NANOS);
    }

    BadgeSnapshotCache(long maxAgeNanos) {
        if (maxAgeNanos < 0) throw new IllegalArgumentException("maxAgeNanos must not be negative");
        this.maxAgeNanos = maxAgeNanos;
    }

    synchronized void record(Collection<UUID> activeRuntimeUuids, long nowNanos) {
        lastKnownGood = bounded(activeRuntimeUuids);
        recordedAtNanos = nowNanos;
        populated = true;
    }

    synchronized Set<UUID> fallback(
        Collection<UUID> visibleRuntimeUuids,
        Collection<UUID> selfIdentities,
        long nowNanos
    ) {
        LinkedHashSet<UUID> fallback = new LinkedHashSet<>();
        if (selfIdentities != null) {
            for (UUID selfIdentity : selfIdentities) {
                if (selfIdentity != null) fallback.add(selfIdentity);
            }
        }
        long ageNanos = nowNanos - recordedAtNanos;
        if (populated && ageNanos >= 0 && ageNanos <= maxAgeNanos && visibleRuntimeUuids != null) {
            for (UUID playerUuid : visibleRuntimeUuids) {
                if (playerUuid != null && lastKnownGood.contains(playerUuid)) fallback.add(playerUuid);
            }
        }
        return bounded(fallback);
    }

    synchronized void clear() {
        lastKnownGood = Set.of();
        recordedAtNanos = 0;
        populated = false;
    }

    private static Set<UUID> bounded(Collection<UUID> playerUuids) {
        if (playerUuids == null || playerUuids.isEmpty()) return Set.of();
        LinkedHashSet<UUID> bounded = new LinkedHashSet<>();
        for (UUID playerUuid : playerUuids) {
            if (playerUuid == null) continue;
            bounded.add(playerUuid);
            if (bounded.size() >= BadgeIdentityAliases.MAX_VISIBLE_PLAYERS) break;
        }
        return Set.copyOf(bounded);
    }
}
