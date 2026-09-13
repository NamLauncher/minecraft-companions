// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.junit.jupiter.api.Test;

final class ServerNameTagTargetTest {
    @Test void serverLabelInjectionDescriptorsMatchMinecraft() throws ReflectiveOperationException {
        assertEquals(void.class, EntityRenderer.class.getDeclaredMethod(
            "extractRenderState", Entity.class, EntityRenderState.class, float.class).getReturnType());
        assertEquals(Component.class, EntityRenderState.class.getField("nameTag").getType());
        assertEquals(void.class, DisplayRenderer.TextDisplayRenderer.class.getDeclaredMethod(
            "extractRenderState", Display.TextDisplay.class, TextDisplayEntityRenderState.class, float.class).getReturnType());
        assertEquals(Display.TextDisplay.CachedInfo.class, DisplayRenderer.TextDisplayRenderer.class.getDeclaredMethod(
            "splitLines", Component.class, int.class).getReturnType());
        assertEquals(Display.TextDisplay.CachedInfo.class, TextDisplayEntityRenderState.class.getField("cachedInfo").getType());
        assertEquals(Display.RenderState.class, TextDisplayEntityRenderState.class.getField("renderState").getType());
        assertEquals(void.class, ClientPacketListener.class.getDeclaredMethod("sendCommand", String.class).getReturnType());
        assertEquals(void.class, ClientPacketListener.class.getDeclaredMethod(
            "sendUnattendedCommand", String.class, Screen.class).getReturnType());
        assertEquals(void.class, ClientPacketListener.class.getDeclaredMethod(
            "handleCommands", ClientboundCommandsPacket.class).getReturnType());
        assertEquals(void.class, ChatComponent.class.getDeclaredMethod(
            "addServerSystemMessage", Component.class).getReturnType());
        assertEquals(void.class, ChatComponent.class.getDeclaredMethod(
            "addPlayerMessage", Component.class, MessageSignature.class, GuiMessageTag.class).getReturnType());
    }

    @Test void cameraFacingStatePreservesEveryServerOwnedRenderProperty() {
        var original = new Display.RenderState(
            null,
            Display.BillboardConstraints.FIXED,
            7,
            null,
            null,
            11
        );
        var cameraFacing = TextDisplayBadgeRenderState.faceCamera(original);

        assertEquals(Display.BillboardConstraints.CENTER, cameraFacing.billboardConstraints());
        assertSame(original.transformation(), cameraFacing.transformation());
        assertEquals(original.brightnessOverride(), cameraFacing.brightnessOverride());
        assertSame(original.shadowRadius(), cameraFacing.shadowRadius());
        assertSame(original.shadowStrength(), cameraFacing.shadowStrength());
        assertEquals(original.glowColorOverride(), cameraFacing.glowColorOverride());
        assertSame(cameraFacing, TextDisplayBadgeRenderState.faceCamera(cameraFacing));
    }
}
