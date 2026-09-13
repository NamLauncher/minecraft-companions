// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NamLauncherCompanionClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("namlauncher-game-companion");

    @Override
    public void onInitializeClient() {
        new PlayerBadgeLookupService(LOGGER).start();
        LOGGER.info("NamLauncher game companion {} enabled.", BrandingFormatter.BRAND);
    }
}
