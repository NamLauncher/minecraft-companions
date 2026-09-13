// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.mojang.authlib.GameProfile;
import com.namlauncher.bridge.badge.PlayerChatBadge;
import java.time.Instant;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Decorates the final local rendering while leaving signed chat content untouched. */
@Mixin(value = ChatListener.class, priority = 900)
abstract class ChatListenerMixin {
    @Redirect(
        method = "showMessageToPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;addPlayerMessage("
                + "Lnet/minecraft/network/chat/Component;"
                + "Lnet/minecraft/network/chat/MessageSignature;"
                + "Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"
        ),
        require = 0
    )
    private void namlauncher$badgePlayerChat(
        ChatComponent chat,
        Component displayedMessage,
        MessageSignature signature,
        GuiMessageTag tag,
        ChatType.Bound bound,
        PlayerChatMessage signedMessage,
        Component unsignedContent,
        GameProfile sender,
        boolean onlySecure,
        Instant receivedAt
    ) {
        chat.addPlayerMessage(
            PlayerChatBadge.decorate(
                displayedMessage,
                sender == null ? null : sender.id(),
                sender == null ? null : sender.name()
            ),
            signature,
            tag
        );
    }
}
