// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.namlauncher.bridge.BrandingFormatter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.client.gui.components.debug.DebugEntryVersion", priority = 900)
abstract class DebugEntryVersionMixin {
    @ModifyArg(
        method = "display",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/debug/DebugScreenDisplayer;addPriorityLine(Ljava/lang/String;)V"
        ),
        index = 0,
        require = 0
    )
    private String namlauncher$brandF3Header(String original) {
        return BrandingFormatter.formatF3Header(original);
    }
}
