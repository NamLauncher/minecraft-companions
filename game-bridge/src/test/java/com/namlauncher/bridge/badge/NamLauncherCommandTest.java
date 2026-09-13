// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.junit.jupiter.api.Test;

final class NamLauncherCommandTest {
    @Test void recognizesOnlyTheLocalCommandRoot() {
        assertTrue(NamLauncherCommand.isLocalCommand("namlauncher"));
        assertTrue(NamLauncherCommand.isLocalCommand(" /NamLauncher "));
        assertTrue(NamLauncherCommand.isLocalCommand("namlauncher help"));
        assertFalse(NamLauncherCommand.isLocalCommand("namlauncherx"));
        assertFalse(NamLauncherCommand.isLocalCommand("minecraft:namlauncher"));
        assertFalse(NamLauncherCommand.isLocalCommand(null));
    }

    @Test void installsAVisibleNonOpCommandAndPreservesServerCommands() throws Exception {
        CommandDispatcher<ClientSuggestionProvider> server = new CommandDispatcher<>();
        server.register(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("servercommand"));
        server.register(LiteralArgumentBuilder.<ClientSuggestionProvider>literal("namlauncher")
            .requires(source -> false)
            .then(LiteralArgumentBuilder.literal("server-child")));

        CommandDispatcher<ClientSuggestionProvider> installed = NamLauncherCommand.install(server);
        var local = installed.getRoot().getChild(NamLauncherCommand.ROOT);
        assertNotNull(local);
        assertTrue(local.canUse(null), "The local command must never depend on server OP permissions");
        assertNotNull(local.getChild("chat-icon"));
        assertNotNull(local.getChild("chat-icon").getChild("on"));
        assertNotNull(local.getChild("chat-icon").getChild("off"));
        assertNotNull(local.getChild("chat-icon").getChild("status"));
        assertNotNull(local.getChild("server-child"), "Do not discard a same-name server command subtree");
        assertNotNull(installed.getRoot().getChild("servercommand"));

        var parsed = installed.parse("nam", (ClientSuggestionProvider) null);
        List<String> suggestions = installed.getCompletionSuggestions(parsed).get().getList().stream()
            .map(suggestion -> suggestion.getText())
            .toList();
        assertEquals(List.of("namlauncher"), suggestions);
    }

    @Test void listsEveryVisibleOldAndCurrentProtocolMatchFromTheSharedRegistry() {
        UUID legacy = UUID.fromString("10000000-0000-4000-8000-000000000001");
        UUID current = UUID.fromString("20000000-0000-4000-8000-000000000001");
        UUID unrelated = UUID.fromString("30000000-0000-4000-8000-000000000001");
        BadgeRegistry.replace(List.of(legacy, current));
        try {
            assertEquals(
                List.of("CurrentPlayer", "LegacyPlayer"),
                NamLauncherCommand.activePlayerNames(List.of(
                    new BadgeIdentityAliases.VisiblePlayer(legacy, "LegacyPlayer"),
                    new BadgeIdentityAliases.VisiblePlayer(current, "CurrentPlayer"),
                    new BadgeIdentityAliases.VisiblePlayer(unrelated, "OtherPlayer")
                ))
            );
        } finally {
            BadgeRegistry.clear();
        }
    }
}
