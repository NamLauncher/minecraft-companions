// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201.mixin;
import com.namlauncher.bridge.forge1201.BrandingFormatter;
import java.util.List;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(DebugScreenOverlay.class)
abstract class DebugScreenOverlayMixin {
    @Inject(method = "getGameInformation", at = @At("RETURN"), cancellable = true)
    private void namlauncher$brandF3(CallbackInfoReturnable<List<String>> callback) { callback.setReturnValue(BrandingFormatter.f3Lines(callback.getReturnValue())); }
}
