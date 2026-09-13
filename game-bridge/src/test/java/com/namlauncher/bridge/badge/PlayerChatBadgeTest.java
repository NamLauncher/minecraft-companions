// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class PlayerChatBadgeTest {
    private static final UUID BADGED = UUID.fromString("12345678-1234-4234-9234-123456789abc");
    private static final UUID OTHER = UUID.fromString("87654321-4321-4321-8321-cba987654321");

    @AfterEach
    void resetRegistry() {
        BadgeRegistry.clear();
        ChatBadgePreference.configure(null);
    }

    @Test
    void decoratesOnlyAConfirmedNamLauncherSender() {
        Component original = Component.literal("<Nattapat2871> hello");
        BadgeRegistry.replace(List.of(BADGED));

        assertEquals(
            "<\uE001 Nattapat2871> hello",
            PlayerChatBadge.decorate(original, BADGED, "Nattapat2871").getString()
        );
        assertSame(original, PlayerChatBadge.decorate(original, OTHER, "Nattapat2871"));
    }

    @Test
    void isNullSafeAndIdempotent() {
        BadgeRegistry.replace(List.of(BADGED));
        assertNull(PlayerChatBadge.decorate(null, BADGED, "Nattapat2871"));

        Component plainMessage = Component.literal("hello");
        assertSame(plainMessage, PlayerChatBadge.decorate(plainMessage, BADGED, "Nattapat2871"));

        Component decorated = PlayerChatBadge.decorate(
            Component.literal("<Nattapat2871> hello"),
            BADGED,
            "Nattapat2871"
        );
        assertSame(decorated, PlayerChatBadge.decorate(decorated, BADGED, "Nattapat2871"));
    }

    @Test
    void disablingChatIconsDoesNotChangeTheSharedBadgeRegistry() {
        Component original = Component.literal("<Nattapat2871> hello");
        BadgeRegistry.replace(List.of(BADGED));
        ChatBadgePreference.configure(null);
        ChatBadgePreference.setEnabled(false);

        assertSame(original, PlayerChatBadge.decorate(original, BADGED, "Nattapat2871"));
        assertSame(original, PlayerChatBadge.decorateServerFormatted(original));
        assertEquals("\uE001 <Nattapat2871> hello", BadgeComponents.prepend(original).getString());
    }
}
