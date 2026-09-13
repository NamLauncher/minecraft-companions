// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

/** A local command: it never sends command text or player data to the connected server. */
public final class NamLauncherCommand {
    public static final String ROOT = "namlauncher";

    private NamLauncherCommand() { }

    /**
     * Add the local root before copying server nodes. This keeps it visible to
     * non-operators even if a server happens to expose an OP-only node with the
     * same name, while preserving every unrelated server command and child.
     */
    public static CommandDispatcher<ClientSuggestionProvider> install(
        CommandDispatcher<ClientSuggestionProvider> serverCommands
    ) {
        CommandDispatcher<ClientSuggestionProvider> combined = new CommandDispatcher<>();
        combined.register(LiteralArgumentBuilder.<ClientSuggestionProvider>literal(ROOT)
            .executes(context -> {
                handle(ROOT);
                return 1;
            })
            .then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("chat-icon")
                .executes(context -> {
                    handle(ROOT + " chat-icon status");
                    return 1;
                })
                .then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("on")
                    .executes(context -> {
                        handle(ROOT + " chat-icon on");
                        return 1;
                    }))
                .then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("off")
                    .executes(context -> {
                        handle(ROOT + " chat-icon off");
                        return 1;
                    }))
                .then(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("status")
                    .executes(context -> {
                        handle(ROOT + " chat-icon status");
                        return 1;
                    }))));
        if (serverCommands != null) {
            for (var command : serverCommands.getRoot().getChildren()) {
                combined.getRoot().addChild(command);
            }
        }
        return combined;
    }

    public static boolean isLocalCommand(String rawCommand) {
        if (rawCommand == null) return false;
        String normalized = rawCommand.stripLeading();
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        return normalized.equalsIgnoreCase(ROOT)
            || (normalized.length() > ROOT.length()
                && normalized.regionMatches(true, 0, ROOT, 0, ROOT.length())
                && Character.isWhitespace(normalized.charAt(ROOT.length())));
    }

    public static boolean handle(String rawCommand) {
        if (!isLocalCommand(rawCommand)) return false;
        Minecraft minecraft = Minecraft.getInstance();
        String normalized = rawCommand.strip();
        if (normalized.startsWith("/")) normalized = normalized.substring(1).stripLeading();
        String arguments = normalized.length() <= ROOT.length()
            ? ""
            : normalized.substring(ROOT.length()).strip();
        if (!arguments.isEmpty()) {
            switch (arguments.toLowerCase(java.util.Locale.ROOT)) {
                case "chat-icon on" -> updateChatIconPreference(minecraft, true);
                case "chat-icon off" -> updateChatIconPreference(minecraft, false);
                case "chat-icon", "chat-icon status" -> show(
                    minecraft,
                    "NamLauncher: ไอคอนในแชท " + (ChatBadgePreference.isEnabled() ? "เปิดอยู่" : "ปิดอยู่")
                );
                default -> show(
                    minecraft,
                    "NamLauncher: วิธีใช้ /namlauncher หรือ /namlauncher chat-icon on|off|status"
                );
            }
            return true;
        }
        var connection = minecraft.getConnection();
        if (connection == null) {
            show(minecraft, "NamLauncher: คำสั่งนี้ใช้ได้เมื่อเข้าเซิร์ฟเวอร์แล้ว");
            return true;
        }
        List<String> names = activePlayerNames(connection.getOnlinePlayers().stream()
            .filter(info -> info != null && info.getProfile() != null)
            .map(info -> new BadgeIdentityAliases.VisiblePlayer(
                info.getProfile().id(),
                info.getProfile().name()
            ))
            .toList());
        if (names.isEmpty()) {
            show(minecraft, "NamLauncher: ยังไม่พบผู้ใช้ NamLauncher ในรายชื่อผู้เล่นขณะนี้");
            return true;
        }
        show(minecraft, "NamLauncher: พบ " + names.size() + " คน");
        StringBuilder line = new StringBuilder();
        for (String name : names) {
            String addition = line.isEmpty() ? name : ", " + name;
            if (!line.isEmpty() && line.length() + addition.length() > 180) {
                show(minecraft, line.toString());
                line.setLength(0);
                addition = name;
            }
            line.append(addition);
        }
        if (!line.isEmpty()) show(minecraft, line.toString());
        return true;
    }

    private static void updateChatIconPreference(Minecraft minecraft, boolean enabled) {
        boolean persisted = ChatBadgePreference.setEnabled(enabled);
        String state = enabled ? "เปิด" : "ปิด";
        show(
            minecraft,
            "NamLauncher: " + state + "ไอคอนในแชทแล้ว"
                + (persisted ? "" : " (ใช้ได้ในรอบนี้ แต่บันทึกค่าถาวรไม่สำเร็จ)")
        );
    }

    static List<String> activePlayerNames(
        Collection<BadgeIdentityAliases.VisiblePlayer> visiblePlayers
    ) {
        if (visiblePlayers == null || visiblePlayers.isEmpty()) return List.of();
        return visiblePlayers.stream()
            .filter(player -> player != null && BadgeRegistry.hasBadge(player.runtimeUuid()))
            .map(BadgeIdentityAliases.VisiblePlayer::playerName)
            .filter(name -> name != null && name.matches("[A-Za-z0-9_]{1,16}"))
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder()))
            .toList();
    }

    private static void show(Minecraft minecraft, String message) {
        NamLauncherChatOutput.show(minecraft, message);
    }
}
