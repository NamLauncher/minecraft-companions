// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class BridgeContractTest {
    private static String readProjectFile(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath), StandardCharsets.UTF_8);
    }

    @Test
    void versionGateIsExact() {
        assertTrue(BrandingConstants.supportsMinecraftVersion("26.2"));
        assertFalse(BrandingConstants.supportsMinecraftVersion("26.2.1"));
        assertFalse(BrandingConstants.supportsMinecraftVersion("26.3"));
        assertFalse(BrandingConstants.supportsMinecraftVersion("1.21.11"));
    }

    @Test
    void metadataPinsMinecraftAndRequiresCompatibleLoaderMinimum() throws IOException {
        String metadata = readProjectFile("src/main/resources/fabric.mod.json");
        assertTrue(metadata.contains("\"fabricloader\": \">=${loader_version}\""));
        assertTrue(metadata.contains("\"minecraft\": \"${minecraft_version}\""));
        assertFalse(metadata.contains("~${minecraft_version}"));
        assertFalse(metadata.contains("~${loader_version}"));
        assertTrue(metadata.contains("\"environment\": \"client\""));
    }

    @Test
    void mixinsAreOptionalAndZeroRequire() throws IOException {
        String mixins = readProjectFile(
            "src/client/resources/namlauncher-branding-bridge.client.mixins.json"
        );
        assertTrue(mixins.contains("\"required\": false"));
        assertTrue(mixins.contains("\"defaultRequire\": 0"));
        assertTrue(mixins.contains("\"MinecraftWindowTitleMixin\""));
        assertTrue(mixins.contains("\"DebugEntryVersionMixin\""));
        assertTrue(mixins.contains("\"DebugEntryFpsMixin\""));
        assertTrue(mixins.contains("\"ChatListenerMixin\""));
        assertTrue(mixins.contains("\"PlayerTabOverlayMixin\""));
        assertTrue(mixins.contains("\"ClientPacketListenerMixin\""));
        assertTrue(mixins.contains("\"AvatarRendererMixin\""));
        assertTrue(mixins.contains(".BrandingMixinPlugin\""));
    }

    @Test
    void bundlesAPrivateFontGlyphInsteadOfUsingRawGraphicsHooks() throws IOException {
        String font = readProjectFile("src/main/resources/assets/namlauncher/font/badge.json");
        String build = readProjectFile("build.gradle");
        assertTrue(font.contains("namlauncher:font/badge.png"));
        assertTrue(font.contains("\\ue001"));
        assertTrue(build.contains("../assets/NamLauncher-icon.png"));
    }
}
