// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.namlauncher.bridge.badge.ServerNameTagBadge;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, priority = 900)
abstract class ServerNameTagMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void namlauncher$serverLabel(Entity entity, EntityRenderState state, float tick, CallbackInfo callback) {
        if (entity instanceof ArmorStand && entity.isInvisible() && entity.isCustomNameVisible() && state.nameTag != null) {
            state.nameTag = ServerNameTagBadge.decorate(entity, state.nameTag);
        }
    }
}
