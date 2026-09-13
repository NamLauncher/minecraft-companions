// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201.mixin;

import com.namlauncher.bridge.fabric1201.BadgeComponents;
import com.namlauncher.bridge.fabric1201.BadgeRegistry;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
abstract class PlayerTabOverlayMixin {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true, require = 0)
    private void namlauncher$tab(PlayerInfo info, CallbackInfoReturnable<Component> callback) {
        if (info != null && BadgeRegistry.has(info.getProfile().getId())) {
            callback.setReturnValue(BadgeComponents.prepend(callback.getReturnValue()));
        }
    }
}
