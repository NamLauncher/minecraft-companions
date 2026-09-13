// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class PlayerBadgeConfigTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void acceptsOnlyThePinnedHostedLookupOrLoopbackDevelopmentEndpoint() {
        assertTrue(PlayerBadgeConfig.isAllowedEndpoint(URI.create(
            "https://namlauncher.nattapat2871.me/api/player-badges/lookup"
        )));
        assertTrue(PlayerBadgeConfig.isAllowedEndpoint(URI.create(
            "http://127.0.0.1:10006/api/player-badges/lookup"
        )));
        assertFalse(PlayerBadgeConfig.isAllowedEndpoint(URI.create(
            "https://attacker.invalid/api/player-badges/lookup"
        )));
        assertFalse(PlayerBadgeConfig.isAllowedEndpoint(URI.create(
            "https://namlauncher.nattapat2871.me/api/player-badges/lookup?server=secret"
        )));
    }

    @Test
    void failsClosedForMalformedOrOversizedConfiguration() throws Exception {
        Path config = temporaryDirectory.resolve("badge.json");
        Files.writeString(config, "{not-json", StandardCharsets.UTF_8);
        assertFalse(PlayerBadgeConfig.load(config).enabled());
        Files.write(config, new byte[16 * 1024 + 1]);
        assertFalse(PlayerBadgeConfig.load(config).enabled());
    }
}
