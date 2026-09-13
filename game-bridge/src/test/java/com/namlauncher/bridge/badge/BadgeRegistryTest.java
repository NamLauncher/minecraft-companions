// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class BadgeRegistryTest {
    private static final UUID PLAYER = UUID.fromString("12345678-1234-4234-9234-123456789abc");

    @AfterEach
    void reset() {
        BadgeRegistry.clear();
    }

    @Test
    void atomicallyReplacesAndClearsDecorativeBadgeState() {
        assertFalse(BadgeRegistry.hasBadge(PLAYER));
        BadgeRegistry.replace(List.of(PLAYER));
        assertTrue(BadgeRegistry.hasBadge(PLAYER));
        var unchangedSnapshot = BadgeRegistry.snapshot();
        BadgeRegistry.replace(List.of(PLAYER));
        assertSame(unchangedSnapshot, BadgeRegistry.snapshot(), "Unchanged lookups must not invalidate render caches");
        BadgeRegistry.clear();
        assertFalse(BadgeRegistry.hasBadge(PLAYER));
    }

    @Test
    void boundsRemoteStateToOneLookupBatch() {
        BadgeRegistry.replace(java.util.stream.IntStream.range(0, 200)
            .mapToObj(index -> new UUID(0, index + 1L))
            .toList());
        assertTrue(BadgeRegistry.size() <= BadgeIdentityAliases.MAX_VISIBLE_PLAYERS);
    }
}
