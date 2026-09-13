// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.namlauncher.bridge.RuntimeVersionGate;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class BrandingMixinPlugin implements IMixinConfigPlugin {
    private static final Set<String> ALLOWED_TARGETS = Set.of(
        "net.minecraft.client.Minecraft",
        "net.minecraft.server.packs.resources.MultiPackResourceManager",
        "net.minecraft.client.gui.components.debug.DebugEntryVersion",
        "net.minecraft.client.gui.components.debug.DebugEntryFps",
        "net.minecraft.client.gui.components.PlayerTabOverlay",
        "net.minecraft.client.gui.components.ChatComponent",
        "net.minecraft.client.multiplayer.ClientPacketListener",
        "net.minecraft.client.renderer.entity.EntityRenderer",
        "net.minecraft.client.renderer.entity.DisplayRenderer$TextDisplayRenderer",
        "net.minecraft.client.renderer.entity.player.AvatarRenderer"
    );

    @Override
    public void onLoad(String mixinPackage) {
        // No eager side effects. The runtime version is checked for each mixin.
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return ALLOWED_TARGETS.contains(targetClassName) && RuntimeVersionGate.isSupportedRuntime();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // Target ownership is deliberately fixed by ALLOWED_TARGETS.
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
        String targetClassName,
        ClassNode targetClass,
        String mixinClassName,
        IMixinInfo mixinInfo
    ) {
        // No bytecode mutation outside the declared Mixin injectors.
    }

    @Override
    public void postApply(
        String targetClassName,
        ClassNode targetClass,
        String mixinClassName,
        IMixinInfo mixinInfo
    ) {
        // No bytecode mutation outside the declared Mixin injectors.
    }
}
