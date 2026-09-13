// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.namlauncher.bridge.badge.BadgeComponents;
import com.namlauncher.bridge.badge.BadgeRegistry;
import com.namlauncher.bridge.badge.TabListBadge;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerTabOverlay.class, priority = 900)
abstract class PlayerTabOverlayMixin {
    @Unique private final Map<PlayerInfo, NamLauncherCachedTabName> namlauncher$tabNameCache = new WeakHashMap<>();

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true, require = 0)
    private void namlauncher$badgeTabList(
        PlayerInfo playerInfo,
        CallbackInfoReturnable<Component> callback
    ) {
        if (playerInfo == null || playerInfo.getProfile() == null) return;
        Set<UUID> activeBadges = BadgeRegistry.snapshot();
        if (!activeBadges.contains(playerInfo.getProfile().id())) {
            namlauncher$tabNameCache.remove(playerInfo);
            return;
        }

        Component original = callback.getReturnValue();
        if (original == null) return;
        NamLauncherCachedTabName cached = namlauncher$tabNameCache.get(playerInfo);
        if (cached == null || cached.activeBadges != activeBadges || !cached.original.equals(original)) {
            cached = new NamLauncherCachedTabName(activeBadges, original, BadgeComponents.prepend(original));
            namlauncher$tabNameCache.put(playerInfo, cached);
        }
        callback.setReturnValue(cached.decorated);
    }

    /**
     * Reapply the icon to the final render entry. Some Tab List integrations
     * replace getNameForDisplay's return value after the first hook runs.
     */
    @ModifyArg(
        method = {"render", "extractRenderState"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay$ScoreDisplayEntry;<init>(Lnet/minecraft/network/chat/Component;ILnet/minecraft/network/chat/Component;I)V"
        ),
        index = 0,
        require = 0
    )
    private Component namlauncher$badgeFinalTabEntry(Component finalName) {
        return TabListBadge.decorate(finalName);
    }

    @Unique
    private static final class NamLauncherCachedTabName {
        private final Set<UUID> activeBadges;
        private final Component original;
        private final Component decorated;

        private NamLauncherCachedTabName(Set<UUID> activeBadges, Component original, Component decorated) {
            this.activeBadges = activeBadges;
            this.original = original;
            this.decorated = decorated;
        }
    }
}
