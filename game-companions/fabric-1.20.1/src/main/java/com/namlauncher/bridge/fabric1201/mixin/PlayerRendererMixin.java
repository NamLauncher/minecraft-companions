// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.namlauncher.bridge.fabric1201.BadgeComponents;
import com.namlauncher.bridge.fabric1201.BadgeRegistry;
import java.util.UUID;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin {
    @Unique
    private UUID namlauncher$currentPlayer;

    @Inject(method = "renderNameTag", at = @At("HEAD"), require = 0)
    private void namlauncher$capture(
        AbstractClientPlayer player,
        Component name,
        PoseStack pose,
        MultiBufferSource buffers,
        int light,
        CallbackInfo callback
    ) {
        namlauncher$currentPlayer = player == null ? null : player.getUUID();
    }

    @ModifyArg(
        method = "renderNameTag",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
        ),
        index = 1,
        require = 0
    )
    private Component namlauncher$decorate(Component original) {
        return BadgeRegistry.has(namlauncher$currentPlayer) ? BadgeComponents.prepend(original) : original;
    }

    @Inject(method = "renderNameTag", at = @At("RETURN"), require = 0)
    private void namlauncher$clear(
        AbstractClientPlayer player,
        Component name,
        PoseStack pose,
        MultiBufferSource buffers,
        int light,
        CallbackInfo callback
    ) {
        namlauncher$currentPlayer = null;
    }
}
