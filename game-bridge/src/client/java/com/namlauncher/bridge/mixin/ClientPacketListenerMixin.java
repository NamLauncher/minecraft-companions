// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.namlauncher.bridge.badge.NamLauncherCommand;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class, priority = 900)
abstract class ClientPacketListenerMixin {
    @Shadow private CommandDispatcher<ClientSuggestionProvider> commands;

    @Inject(method = "handleCommands", at = @At("TAIL"), require = 0)
    private void namlauncher$registerLocalCommand(ClientboundCommandsPacket packet, CallbackInfo callback) {
        commands = NamLauncherCommand.install(commands);
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true, require = 0)
    private void namlauncher$localCommand(String command, CallbackInfo callback) {
        if (NamLauncherCommand.handle(command)) callback.cancel();
    }

    @Inject(method = "sendUnattendedCommand", at = @At("HEAD"), cancellable = true, require = 0)
    private void namlauncher$localUnattendedCommand(String command, Screen screen, CallbackInfo callback) {
        if (NamLauncherCommand.handle(command)) callback.cancel();
    }
}
