// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.namlauncher.bridge.badge.BadgeComponents;
import com.namlauncher.bridge.badge.BadgeRegistry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AvatarRenderer.class, priority = 900)
abstract class AvatarRendererMixin {
    @Unique private final Map<Avatar, NamLauncherCachedNameTag> namlauncher$nameTagCache = new WeakHashMap<>();

    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void namlauncher$badgeNameTag(
        Avatar avatar,
        AvatarRenderState renderState,
        float partialTick,
        CallbackInfo callback
    ) {
        if (avatar == null || renderState == null || renderState.nameTag == null) return;
        Set<UUID> activeBadges = BadgeRegistry.snapshot();
        if (!activeBadges.contains(avatar.getUUID())) {
            namlauncher$nameTagCache.remove(avatar);
            return;
        }

        Component original = renderState.nameTag;
        NamLauncherCachedNameTag cached = namlauncher$nameTagCache.get(avatar);
        if (cached == null || cached.activeBadges != activeBadges || !cached.original.equals(original)) {
            cached = new NamLauncherCachedNameTag(activeBadges, original, BadgeComponents.prepend(original));
            namlauncher$nameTagCache.put(avatar, cached);
        }
        renderState.nameTag = cached.decorated;
    }

    @Unique
    private static final class NamLauncherCachedNameTag {
        private final Set<UUID> activeBadges;
        private final Component original;
        private final Component decorated;

        private NamLauncherCachedNameTag(Set<UUID> activeBadges, Component original, Component decorated) {
            this.activeBadges = activeBadges;
            this.original = original;
            this.decorated = decorated;
        }
    }
}
