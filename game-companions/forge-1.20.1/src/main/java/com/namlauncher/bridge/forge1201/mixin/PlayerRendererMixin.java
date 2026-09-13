// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.namlauncher.bridge.forge1201.BadgeComponents;
import com.namlauncher.bridge.forge1201.BadgeRegistry;
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
    @Unique private UUID namlauncher$renderedPlayer;
    @Inject(method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void namlauncher$capturePlayer(AbstractClientPlayer player, Component component, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo callback) { namlauncher$renderedPlayer = player == null ? null : player.getUUID(); }
    @ModifyArg(method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"), index = 1)
    private Component namlauncher$badgeNameTag(Component original) { return BadgeRegistry.isActive(namlauncher$renderedPlayer) ? BadgeComponents.prepend(original) : original; }
    @Inject(method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void namlauncher$clearPlayer(AbstractClientPlayer player, Component component, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo callback) { namlauncher$renderedPlayer = null; }
}
