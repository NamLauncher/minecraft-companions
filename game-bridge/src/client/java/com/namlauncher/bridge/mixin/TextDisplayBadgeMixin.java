// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.namlauncher.bridge.badge.ServerNameTagBadge;
import com.namlauncher.bridge.badge.TextDisplayBadgeRenderState;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DisplayRenderer.TextDisplayRenderer.class, priority = 900)
abstract class TextDisplayBadgeMixin {
    @Shadow private Display.TextDisplay.CachedInfo splitLines(Component text, int width) { throw new AssertionError(); }
    @Unique private final Map<Component, NamLauncherCachedLines> namlauncher$lineCache = new WeakHashMap<>();

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V", at = @At("TAIL"), require = 0)
    private void namlauncher$textLabel(Display.TextDisplay entity, TextDisplayEntityRenderState state, float tick, CallbackInfo callback) {
        if (state.textRenderState == null || state.cachedInfo == null) return;
        Component original = state.textRenderState.text();
        Component decorated = ServerNameTagBadge.decorate(entity, original);
        if (decorated != original) {
            // Change only this frame, not the entity's server-owned text or line cache.
            int lineWidth = state.textRenderState.lineWidth();
            NamLauncherCachedLines cached = namlauncher$lineCache.get(decorated);
            if (cached == null || cached.lineWidth != lineWidth) {
                cached = new NamLauncherCachedLines(lineWidth, splitLines(decorated, lineWidth));
                namlauncher$lineCache.put(decorated, cached);
            }
            state.cachedInfo = cached.info;
            state.renderState = cached.faceCamera(state.renderState);
        }
    }

    @Unique
    private static final class NamLauncherCachedLines {
        private final int lineWidth;
        private final Display.TextDisplay.CachedInfo info;
        private Display.RenderState sourceRenderState;
        private Display.RenderState cameraFacingRenderState;

        private NamLauncherCachedLines(int lineWidth, Display.TextDisplay.CachedInfo info) {
            this.lineWidth = lineWidth;
            this.info = info;
        }

        private Display.RenderState faceCamera(Display.RenderState renderState) {
            if (sourceRenderState == renderState
                || (sourceRenderState != null && sourceRenderState.equals(renderState))) {
                return cameraFacingRenderState;
            }
            sourceRenderState = renderState;
            cameraFacingRenderState = TextDisplayBadgeRenderState.faceCamera(renderState);
            return cameraFacingRenderState;
        }
    }
}
