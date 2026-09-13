// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.namlauncher.bridge.badge.PlayerChatBadge;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Decorates final 26.2 chat rows, including plugin-formatted system chat. */
@Mixin(value = ChatComponent.class, priority = 900)
abstract class ChatComponentBadgeMixin {
    @ModifyVariable(
        method = "addServerSystemMessage(Lnet/minecraft/network/chat/Component;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0
    )
    private Component namlauncher$badgeServerChat(Component message) {
        return PlayerChatBadge.decorateServerFormatted(message);
    }

    @ModifyVariable(
        method = "addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0
    )
    private Component namlauncher$badgeDisguisedPlayerChat(Component message) {
        return PlayerChatBadge.decorateServerFormatted(message);
    }
}
