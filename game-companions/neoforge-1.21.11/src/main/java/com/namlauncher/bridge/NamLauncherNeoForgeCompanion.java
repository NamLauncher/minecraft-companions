// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;
import com.namlauncher.bridge.badge.ChatBadgePreference;
import com.namlauncher.bridge.badge.PlayerBadgeLookupService;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Mod(value = "namlauncher_game_companion", dist = Dist.CLIENT)
public final class NamLauncherNeoForgeCompanion {
    private static final Logger LOGGER = LoggerFactory.getLogger("namlauncher-game-companion");
    public NamLauncherNeoForgeCompanion() {
        ChatBadgePreference.configure(FMLPaths.GAMEDIR.get());
        new PlayerBadgeLookupService(LOGGER, FMLPaths.GAMEDIR.get()).start();
        LOGGER.info("NamLauncher game companion {} enabled for Minecraft {}.", BrandingConstants.LAUNCHER_VERSION, BrandingConstants.TARGET_MINECRAFT_VERSION);
    }
}
