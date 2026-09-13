// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Minecraft 26.2 chat adapter. */
final class NamLauncherChatOutput {
    private NamLauncherChatOutput() { }

    static void show(Minecraft minecraft, String message) {
        minecraft.gui.chatListener().handleSystemMessage(Component.literal(message), false);
    }
}
