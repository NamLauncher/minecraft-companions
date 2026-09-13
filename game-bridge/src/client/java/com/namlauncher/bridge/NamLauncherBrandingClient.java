// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;

import com.namlauncher.bridge.badge.PlayerBadgeLookupService;
import com.namlauncher.bridge.badge.ChatBadgePreference;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NamLauncherBrandingClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrandingConstants.MOD_ID);

    @Override
    public void onInitializeClient() {
        if (RuntimeVersionGate.isSupportedRuntime()) {
            ChatBadgePreference.configure(FabricLoader.getInstance().getGameDir());
            new PlayerBadgeLookupService(LOGGER).start();
            LOGGER.info(
                "NamLauncher branding and player badge bridge {} enabled for Minecraft {}.",
                BrandingConstants.LAUNCHER_VERSION,
                BrandingConstants.TARGET_MINECRAFT_VERSION
            );
        } else {
            LOGGER.warn("NamLauncher branding bridge disabled: unsupported Minecraft runtime.");
        }
    }
}
