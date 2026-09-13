// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201.mixin;
import com.namlauncher.bridge.forge1201.BrandingFormatter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Minecraft.class)
abstract class MinecraftWindowTitleMixin {
    // Cosmetic branding must not depend on compiler-specific local variables.
    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true, require = 0)
    private void namlauncher$brandWindowTitle(CallbackInfoReturnable<String> callback) {
        callback.setReturnValue(BrandingFormatter.windowTitle(callback.getReturnValue()));
    }
}
