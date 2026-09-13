// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201.mixin;

import com.namlauncher.bridge.forge1201.BadgeResourcePack;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MultiPackResourceManager.class)
abstract class BadgeResourceManagerMixin {
    // A fresh manager is created on every resource reload, including server packs.
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, require = 0)
    private static List<PackResources> namlauncher$protectBadge(
        List<PackResources> packs, PackType type, List<PackResources> original
    ) {
        return BadgeResourcePack.protect(type, packs);
    }
}
