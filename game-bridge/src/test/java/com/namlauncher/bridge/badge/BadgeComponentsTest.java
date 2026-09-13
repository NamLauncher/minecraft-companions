// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.junit.jupiter.api.Test;

final class BadgeComponentsTest {
    @Test void placesBadgeAtAbsoluteLeftAndPreservesServerRanksStylesAndTwoLines() {
        var original = Component.literal("ADMIN\n").withStyle(net.minecraft.ChatFormatting.RED)
            .append(Component.literal("[Guild] Nattapat2871 6ms").withStyle(net.minecraft.ChatFormatting.GREEN));
        assertTrue(BadgeComponents.containsExactlyOnePlayerName(original, "Nattapat2871"));
        var decorated = BadgeComponents.prepend(original);
        assertFalse(BadgeComponents.isDecorated(original));
        assertTrue(BadgeComponents.isDecorated(decorated));
        assertEquals("\uE001 ADMIN\n[Guild] Nattapat2871 6ms", decorated.getString());
        assertEquals("ADMIN\n[Guild] Nattapat2871 6ms", original.getString());
        assertSame(decorated, BadgeComponents.prepend(decorated));
        assertFalse(BadgeComponents.containsExactlyOnePlayerName(original, "Nattapat"));
        var ambiguous = Component.literal("Nattapat2871 / Nattapat2871");
        assertFalse(BadgeComponents.containsExactlyOnePlayerName(ambiguous, "Nattapat2871"));
    }

    @Test void matchesANameSplitAcrossStyledComponents() {
        var original = Component.literal("VIP ").append(Component.literal("Nattapat").withStyle(net.minecraft.ChatFormatting.RED))
            .append(Component.literal("2871").withStyle(net.minecraft.ChatFormatting.GREEN));
        assertTrue(BadgeComponents.containsExactlyOnePlayerName(original, "Nattapat2871"));
        assertEquals("\uE001 VIP Nattapat2871", BadgeComponents.prepend(original).getString());
    }

    @Test void decoratesTheFinalServerOwnedEntryOnlyForOneKnownPlayer() {
        Component entry = Component.literal("ADMIN ").withStyle(net.minecraft.ChatFormatting.GOLD)
            .append(Component.literal("Nattapat2871").withStyle(net.minecraft.ChatFormatting.RED));

        Component decorated = BadgeComponents.prependForExactlyOnePlayerName(
            entry,
            List.of("Nattapat2871", "AnotherPlayer")
        );

        assertEquals("\uE001 ADMIN Nattapat2871", decorated.getString());
        assertEquals("ADMIN Nattapat2871", entry.getString(), "Server text must remain immutable");
        assertSame(entry, BadgeComponents.prependForExactlyOnePlayerName(entry, List.of("AnotherPlayer")));
        Component ambiguous = Component.literal("Nattapat2871 AnotherPlayer");
        assertSame(ambiguous, BadgeComponents.prependForExactlyOnePlayerName(
            ambiguous,
            List.of("Nattapat2871", "AnotherPlayer")
        ));
    }

    @Test
    void serverRankUsingTheSameGlyphDoesNotHideOurBadge() {
        Component original = Component.literal("\uE001 Rank Player");
        assertEquals("\uE001 \uE001 Rank Player", BadgeComponents.prepend(original).getString());
    }

    @Test
    void prependsOneIconWithoutDestroyingTheOriginalText() {
        Component original = Component.literal("[Team] Nattapat2871");
        Component decorated = BadgeComponents.prepend(original);
        assertEquals(BadgeComponents.GLYPH + " [Team] Nattapat2871", decorated.getString());
        assertEquals(Style.EMPTY, decorated.getStyle());
        assertNotEquals(Style.EMPTY, decorated.getSiblings().getFirst().getStyle());
        assertEquals(Style.EMPTY, decorated.getSiblings().get(1).getStyle());
        assertEquals(" ", decorated.getSiblings().get(1).getString());
        assertEquals(Style.EMPTY, decorated.getSiblings().get(2).getStyle());
        assertSame(decorated, BadgeComponents.prepend(decorated));
    }

    @Test
    void doesNotTreatPartOfAnOversizedTokenAsAPlayerName() {
        Component original = Component.literal("prefixNattapat2871suffix");

        Component decorated = BadgeComponents.prependForExactlyOnePlayerName(
            original,
            List.of("Nattapat2871")
        );

        assertSame(original, decorated);
    }

    @Test
    void decoratesOnlyAPlayerNameInAChatHeader() {
        Component pluginChat = Component.literal("[12:34] [VILLAGER] Nattapat2871 » hello");
        Component decorated = BadgeComponents.insertBadgeBeforeExactlyOnePlayerChatHeaderName(
            pluginChat,
            List.of("Nattapat2871")
        );
        assertEquals("[12:34] [VILLAGER] \uE001 Nattapat2871 » hello", decorated.getString());

        Component vanillaChat = Component.literal("<Nattapat2871> hello");
        assertEquals(
            "<\uE001 Nattapat2871> hello",
            BadgeComponents.insertBadgeBeforeExactlyOnePlayerChatHeaderName(
                vanillaChat,
                List.of("Nattapat2871")
            ).getString()
        );

        Component joined = Component.literal("Nattapat2871 earned achievement: Stone Age");
        assertSame(joined, BadgeComponents.insertBadgeBeforeExactlyOnePlayerChatHeaderName(
            joined,
            List.of("Nattapat2871")
        ));

        Component ambiguous = Component.literal("Nattapat2871: hello AnotherPlayer: hi");
        assertSame(ambiguous, BadgeComponents.insertBadgeBeforeExactlyOnePlayerChatHeaderName(
            ambiguous,
            List.of("Nattapat2871", "AnotherPlayer")
        ));
    }

    @Test
    void insertsTheChatBadgeBesideAStyledPlayerNameAndRemainsIdempotent() {
        Component original = Component.literal("[VILLAGER] <")
            .append(Component.literal("Nattapat").withStyle(net.minecraft.ChatFormatting.RED))
            .append(Component.literal("2871").withStyle(net.minecraft.ChatFormatting.GREEN))
            .append(Component.literal("> hello"));

        Component decorated = BadgeComponents.insertBadgeBeforePlayerChatName(original, "Nattapat2871");

        assertEquals("[VILLAGER] <\uE001 Nattapat2871> hello", decorated.getString());
        assertTrue(BadgeComponents.isDecorated(decorated));
        assertSame(decorated, BadgeComponents.insertBadgeBeforePlayerChatName(decorated, "Nattapat2871"));
        assertEquals("[VILLAGER] <Nattapat2871> hello", original.getString());
    }
}
