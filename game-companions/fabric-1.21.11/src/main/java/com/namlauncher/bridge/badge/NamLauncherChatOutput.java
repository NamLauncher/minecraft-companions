// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Minecraft 1.21.11 chat adapter. */
final class NamLauncherChatOutput {
    private NamLauncherChatOutput() { }

    static void show(Minecraft minecraft, String message) {
        minecraft.gui.getChat().addMessage(Component.literal(message));
    }
}
