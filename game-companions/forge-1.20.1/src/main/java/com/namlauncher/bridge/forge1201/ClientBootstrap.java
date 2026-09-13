// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201;

import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ClientBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger("namlauncher-game-companion");
    private static boolean started;

    private ClientBootstrap() {
    }

    static synchronized void start() {
        if (started) return;
        started = true;
        new PlayerBadgeLookupService(LOGGER, FMLPaths.GAMEDIR.get()).start();
        LOGGER.info("NamLauncher game companion {} enabled.", BrandingFormatter.brand());
    }
}
