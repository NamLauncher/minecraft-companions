// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Local, per-instance preference for showing NamLauncher icons in chat. */
public final class ChatBadgePreference {
    private static final int MAX_CONFIG_BYTES = 4 * 1024;
    private static volatile boolean enabled = true;
    private static volatile Path configPath;

    private ChatBadgePreference() {
    }

    public static synchronized void configure(Path gameDir) {
        configPath = gameDir == null
            ? null
            : gameDir.toAbsolutePath().normalize().resolve("config").resolve("namlauncher-chat-icons.json");
        enabled = readOrDefault(configPath);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /** Updates the current session immediately and returns whether it was persisted. */
    public static synchronized boolean setEnabled(boolean nextEnabled) {
        enabled = nextEnabled;
        if (configPath == null) return false;
        try {
            Path parent = configPath.getParent();
            Files.createDirectories(parent);
            if (Files.isSymbolicLink(configPath)) return false;

            JsonObject root = new JsonObject();
            root.addProperty("schemaVersion", 1);
            root.addProperty("enabled", nextEnabled);
            Path temporary = parent.resolve(configPath.getFileName() + ".tmp");
            Files.writeString(temporary, root + System.lineSeparator(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, configPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean readOrDefault(Path path) {
        try {
            if (path == null || Files.isSymbolicLink(path)
                || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                return true;
            }
            long size = Files.size(path);
            if (size <= 0 || size > MAX_CONFIG_BYTES) return true;
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("schemaVersion") || root.get("schemaVersion").getAsInt() != 1 || !root.has("enabled")) {
                return true;
            }
            return root.get("enabled").getAsBoolean();
        } catch (IOException | RuntimeException ignored) {
            return true;
        }
    }
}
