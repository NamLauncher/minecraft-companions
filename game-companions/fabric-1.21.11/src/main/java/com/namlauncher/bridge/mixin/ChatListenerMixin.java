// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.mojang.authlib.GameProfile;
import com.namlauncher.bridge.badge.PlayerChatBadge;
import java.time.Instant;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Minecraft 1.21.11 chat renderer adapter; signed content is never modified. */
@Mixin(value = ChatListener.class, priority = 900)
abstract class ChatListenerMixin {
    @Redirect(
        method = "showMessageToPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage("
                + "Lnet/minecraft/network/chat/Component;"
                + "Lnet/minecraft/network/chat/MessageSignature;"
                + "Lnet/minecraft/client/GuiMessageTag;)V"
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
        chat.addMessage(
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
