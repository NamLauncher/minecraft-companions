// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;
import com.namlauncher.bridge.badge.ChatBadgePreference;
import com.namlauncher.bridge.badge.PlayerBadgeLookupService;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Mod("namlauncher_game_companion")
public final class NamLauncherForgeCompanion {
    private static final Logger LOGGER = LoggerFactory.getLogger("namlauncher-game-companion");
    public NamLauncherForgeCompanion() {
        ChatBadgePreference.configure(FMLPaths.GAMEDIR.get());
        new PlayerBadgeLookupService(LOGGER, FMLPaths.GAMEDIR.get()).start();
        LOGGER.info("NamLauncher game companion {} enabled for Minecraft {}.", BrandingConstants.LAUNCHER_VERSION, BrandingConstants.TARGET_MINECRAFT_VERSION);
    }
}
