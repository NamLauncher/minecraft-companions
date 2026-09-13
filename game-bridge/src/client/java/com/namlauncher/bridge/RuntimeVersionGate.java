// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;

import net.fabricmc.loader.api.FabricLoader;

public final class RuntimeVersionGate {
    private RuntimeVersionGate() {
    }

    public static boolean isSupportedRuntime() {
        try {
            String version = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("");
            return BrandingConstants.supportsMinecraftVersion(version);
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }
}
