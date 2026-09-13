// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.namlauncher.bridge.BrandingFormatter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 900)
abstract class MinecraftWindowTitleMixin {
    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true, require = 0)
    private void namlauncher$brandWindowTitle(CallbackInfoReturnable<String> callback) {
        String original = callback.getReturnValue();
        String branded = BrandingFormatter.formatWindowTitle(original);
        if (branded != original) {
            callback.setReturnValue(branded);
        }
    }
}
