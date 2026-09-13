// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod("namlauncher_game_companion")
public final class NamLauncherForgeCompanion {
    public NamLauncherForgeCompanion() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientBootstrap::start);
    }
}
