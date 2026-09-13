// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

final class BadgeIdentityAliasesTest {
    @Test void resolvesAStandardOfflineAliasBackToTheServersProxyUuid() {
        UUID runtime = UUID.fromString("00000000-0000-0000-0009-01f3e6af3d37");
        UUID offline = UUID.fromString("d4e9507b-d28d-3907-b82c-b1bc9f12bab1");
        var plan = BadgeIdentityAliases.plan(
            List.of(new BadgeIdentityAliases.VisiblePlayer(runtime, "Nattapat2871")), runtime
        );

        assertEquals(offline, BadgeIdentityAliases.offlinePlayerUuid("Nattapat2871"));
        assertTrue(plan.requestedUuids().contains(runtime));
        // The self entry is always active locally; peers still query this name alias in their own plan.
        var peerPlan = BadgeIdentityAliases.plan(
            List.of(new BadgeIdentityAliases.VisiblePlayer(runtime, "Nattapat2871")), null
        );
        assertTrue(peerPlan.requestedUuids().contains(offline));
        assertEquals(java.util.Set.of(runtime), peerPlan.resolve(List.of(offline)));
    }

    @Test void keepsTheLocalRuntimeUuidWhenAFullTabListIsBounded() {
        UUID self = UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff");
        ArrayList<BadgeIdentityAliases.VisiblePlayer> players = new ArrayList<>();
        IntStream.range(0, BadgeIdentityAliases.MAX_VISIBLE_PLAYERS + 50).forEach(index -> players.add(
            new BadgeIdentityAliases.VisiblePlayer(
                new UUID(0x1234567800004000L + index, 0x8000000000000000L + index),
                "Player" + index
            )
        ));

        var plan = BadgeIdentityAliases.plan(players, self);
        assertTrue(plan.visibleRuntimeUuids().contains(self));
        assertEquals(BadgeIdentityAliases.MAX_VISIBLE_PLAYERS, plan.visibleRuntimeUuids().size());
        assertTrue(plan.requestedUuids().size() <= BadgeIdentityAliases.MAX_QUERY_UUIDS);
    }

    @Test void includesEveryPlayerOnAThreeHundredSlotServer() {
        ArrayList<BadgeIdentityAliases.VisiblePlayer> players = new ArrayList<>();
        IntStream.range(0, 300).forEach(index -> players.add(
            new BadgeIdentityAliases.VisiblePlayer(
                new UUID(0x2234567800004000L + index, 0x8000000000000000L + index),
                "P" + index
            )
        ));

        var plan = BadgeIdentityAliases.plan(players, null);
        assertEquals(300, plan.visibleRuntimeUuids().size());
        assertEquals(600, plan.requestedUuids().size());
    }

    @Test void rejectsNamesThatCannotBeMinecraftAccountAliases() {
        assertNull(BadgeIdentityAliases.offlinePlayerUuid("ชื่อไทย"));
        assertNull(BadgeIdentityAliases.offlinePlayerUuid("seventeen_chars_1"));
    }
}
