// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;
public final class BrandingConstants {
    public static final String MOD_ID = "namlauncher-game-companion";
    public static final String LAUNCHER_VERSION = "1.2.4";
    public static final String TARGET_MINECRAFT_VERSION = "1.21.11";
    public static final String BRAND = "NamLauncher v." + LAUNCHER_VERSION + " (" + TARGET_MINECRAFT_VERSION + ")";
    private BrandingConstants() { }
    public static boolean supportsMinecraftVersion(String version) { return TARGET_MINECRAFT_VERSION.equals(version); }
}
