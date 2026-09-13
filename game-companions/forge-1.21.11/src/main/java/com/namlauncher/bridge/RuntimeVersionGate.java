// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;
import net.minecraft.SharedConstants;
public final class RuntimeVersionGate {
    private RuntimeVersionGate() { }
    public static boolean isSupportedRuntime() {
        try { return BrandingConstants.supportsMinecraftVersion(SharedConstants.getCurrentVersion().name()); }
        catch (RuntimeException | LinkageError ignored) { return false; }
    }
}
