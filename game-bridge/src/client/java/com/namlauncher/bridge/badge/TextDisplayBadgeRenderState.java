// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import net.minecraft.world.entity.Display;

/** Produces a frame-local camera-facing state without mutating the server entity. */
public final class TextDisplayBadgeRenderState {
    private TextDisplayBadgeRenderState() { }

    public static Display.RenderState faceCamera(Display.RenderState original) {
        if (original == null
            || original.billboardConstraints() == Display.BillboardConstraints.CENTER) {
            return original;
        }
        return new Display.RenderState(
            original.transformation(),
            Display.BillboardConstraints.CENTER,
            original.brightnessOverride(),
            original.shadowRadius(),
            original.shadowStrength(),
            original.glowColorOverride()
        );
    }
}
