// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

/** Applies a badge to the final Tab List component without changing server data. */
public final class TabListBadge {
    private static volatile NameCache nameCache = new NameCache(null, Set.of(), Set.of());

    private TabListBadge() { }

    public static Component decorate(Component text) {
        if (BadgeComponents.isDecorated(text)) return text;
        return BadgeComponents.prependForExactlyOnePlayerName(text, activePlayerNames(text));
    }

    public static Component decorateChat(Component text) {
        if (BadgeComponents.isDecorated(text)) return text;
        return BadgeComponents.insertBadgeBeforeExactlyOnePlayerChatHeaderName(text, activePlayerNames(text));
    }

    private static Set<String> activePlayerNames(Component text) {
        var connection = Minecraft.getInstance().getConnection();
        if (text == null || connection == null || text.getString().length() > 512) return Set.of();
        Set<UUID> activeBadges = BadgeRegistry.snapshot();
        NameCache cached = nameCache;
        if (cached.connection() != connection || cached.activeBadges() != activeBadges) {
            cached = new NameCache(connection, activeBadges, connection.getOnlinePlayers().stream()
            .filter(info -> info != null && info.getProfile() != null)
            .filter(info -> BadgeRegistry.hasBadge(info.getProfile().id()))
            .map(info -> info.getProfile().name())
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toUnmodifiableSet()));
            nameCache = cached;
        }
        return cached.playerNames();
    }

    private record NameCache(
        ClientPacketListener connection,
        Set<UUID> activeBadges,
        Set<String> playerNames
    ) { }
}
