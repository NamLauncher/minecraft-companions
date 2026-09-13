// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class BadgeSnapshotCacheTest {
    private static final UUID SELF = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID PEER = UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static final UUID LEFT_SERVER = UUID.fromString("00000000-0000-4000-8000-000000000003");

    @Test void keepsOnlyStillVisiblePlayersForAtMostFortyFiveSeconds() {
        long recordedAt = Duration.ofMinutes(1).toNanos();
        var cache = new BadgeSnapshotCache();
        cache.record(List.of(SELF, PEER, LEFT_SERVER), recordedAt);

        assertEquals(
            Set.of(SELF, PEER),
            cache.fallback(Set.of(SELF, PEER), Set.of(SELF), recordedAt + BadgeSnapshotCache.MAX_AGE_NANOS)
        );
        assertEquals(
            Set.of(SELF),
            cache.fallback(
                Set.of(SELF, PEER),
                Set.of(SELF),
                recordedAt + BadgeSnapshotCache.MAX_AGE_NANOS + 1
            )
        );
    }

    @Test void clearingOnDisconnectOrServerChangeDropsEveryPeerImmediately() {
        var cache = new BadgeSnapshotCache();
        cache.record(Set.of(PEER), 100);
        cache.clear();

        assertEquals(Set.of(SELF), cache.fallback(Set.of(SELF, PEER), Set.of(SELF), 101));
    }

    @Test void aClockValueBeforeTheSnapshotFailsClosed() {
        var cache = new BadgeSnapshotCache();
        cache.record(Set.of(PEER), 200);

        assertEquals(Set.of(SELF), cache.fallback(Set.of(PEER), Set.of(SELF), 199));
    }
}
