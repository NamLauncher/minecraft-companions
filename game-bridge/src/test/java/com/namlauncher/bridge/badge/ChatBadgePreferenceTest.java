// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ChatBadgePreferenceTest {
    @TempDir Path gameDir;

    @AfterEach
    void resetPreference() {
        ChatBadgePreference.configure(null);
    }

    @Test
    void defaultsToEnabledAndPersistsPerInstance() throws Exception {
        ChatBadgePreference.configure(gameDir);
        assertTrue(ChatBadgePreference.isEnabled());

        assertTrue(ChatBadgePreference.setEnabled(false));
        assertFalse(ChatBadgePreference.isEnabled());
        assertTrue(Files.isRegularFile(gameDir.resolve("config/namlauncher-chat-icons.json")));

        ChatBadgePreference.configure(gameDir);
        assertFalse(ChatBadgePreference.isEnabled());

        assertTrue(ChatBadgePreference.setEnabled(true));
        ChatBadgePreference.configure(gameDir);
        assertTrue(ChatBadgePreference.isEnabled());
    }

    @Test
    void malformedOrOversizedConfigFailsOpen() throws Exception {
        Path config = gameDir.resolve("config/namlauncher-chat-icons.json");
        Files.createDirectories(config.getParent());
        Files.writeString(config, "not-json");
        ChatBadgePreference.configure(gameDir);
        assertTrue(ChatBadgePreference.isEnabled());

        Files.writeString(config, "x".repeat(4097));
        ChatBadgePreference.configure(gameDir);
        assertTrue(ChatBadgePreference.isEnabled());
    }
}
