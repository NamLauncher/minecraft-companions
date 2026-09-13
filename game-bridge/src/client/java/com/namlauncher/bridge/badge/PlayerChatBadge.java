// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import java.util.UUID;
import net.minecraft.network.chat.Component;

/** Adds the local NamLauncher badge to a verified player chat sender. */
public final class PlayerChatBadge {
    private PlayerChatBadge() {
    }

    public static Component decorate(Component displayedMessage, UUID senderUuid, String senderName) {
        if (displayedMessage == null || !ChatBadgePreference.isEnabled() || !BadgeRegistry.hasBadge(senderUuid)) {
            return displayedMessage;
        }
        return BadgeComponents.insertBadgeBeforePlayerChatName(displayedMessage, senderName);
    }

    /** Covers plugin-formatted player chat that arrives through system-chat packets. */
    public static Component decorateServerFormatted(Component displayedMessage) {
        return ChatBadgePreference.isEnabled() ? TabListBadge.decorateChat(displayedMessage) : displayedMessage;
    }
}
