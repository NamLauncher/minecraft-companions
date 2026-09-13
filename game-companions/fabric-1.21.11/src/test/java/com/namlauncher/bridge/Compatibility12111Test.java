// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.namlauncher.bridge.badge.BadgeComponents;
import java.io.InputStream;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

final class Compatibility12111Test {
    @Test
    void versionGateIsExact() {
        assertTrue(BrandingConstants.supportsMinecraftVersion("1.21.11"));
    }

    @Test
    void glyphIsPrivateUseCodePoint() {
        assertEquals("\uE001", BadgeComponents.GLYPH);
    }

    @Test
    void localCommandHookMatchesMinecraft() throws ReflectiveOperationException {
        assertEquals(
            void.class,
            ClientPacketListener.class.getDeclaredMethod("sendCommand", String.class).getReturnType()
        );
        assertEquals(
            void.class,
            ClientPacketListener.class.getDeclaredMethod(
                "sendUnattendedCommand",
                String.class,
                Screen.class
            ).getReturnType()
        );
        assertEquals(
            void.class,
            ClientPacketListener.class.getDeclaredMethod(
                "handleCommands",
                ClientboundCommandsPacket.class
            ).getReturnType()
        );
    }

    @Test
    void finalTabEntryHookMatchesMinecraft12111() throws ReflectiveOperationException {
        assertEquals(
            void.class,
            PlayerTabOverlay.class.getDeclaredMethod(
                "render",
                GuiGraphics.class,
                int.class,
                Scoreboard.class,
                Objective.class
            ).getReturnType()
        );
        Class<?> scoreDisplayEntry = Class.forName(
            "net.minecraft.client.gui.components.PlayerTabOverlay$ScoreDisplayEntry",
            false,
            Compatibility12111Test.class.getClassLoader()
        );
        assertNotNull(scoreDisplayEntry.getDeclaredConstructor(
            Component.class,
            int.class,
            Component.class,
            int.class
        ));
    }

    @Test
    void renderStillConstructsTheFinalTabEntryTarget() throws Exception {
        String resource = "net/minecraft/client/gui/components/PlayerTabOverlay.class";
        try (InputStream bytecode = Compatibility12111Test.class
            .getClassLoader()
            .getResourceAsStream(resource)) {
            assertNotNull(bytecode, "Resolved Minecraft 1.21.11 bytecode is missing " + resource);
            ClassNode classNode = new ClassNode();
            new ClassReader(bytecode).accept(classNode, ClassReader.SKIP_FRAMES);
            MethodNode render = classNode.methods.stream()
                .filter(method -> method.name.equals("render"))
                .findFirst()
                .orElseThrow();
            boolean targetExists = false;
            for (AbstractInsnNode instruction : render.instructions) {
                if (instruction instanceof MethodInsnNode invoke
                    && invoke.owner.equals(
                        "net/minecraft/client/gui/components/PlayerTabOverlay$ScoreDisplayEntry"
                    )
                    && invoke.name.equals("<init>")
                    && invoke.desc.equals(
                        "(Lnet/minecraft/network/chat/Component;I"
                            + "Lnet/minecraft/network/chat/Component;I)V"
                    )) {
                    targetExists = true;
                    break;
                }
            }
            assertTrue(targetExists, "Final 1.21.11 Tab List entry Mixin target drifted");
        }
    }

    @Test
    void playerChatHookMatchesMinecraft12111Bytecode() throws Exception {
        String resource = "net/minecraft/client/multiplayer/chat/ChatListener.class";
        try (InputStream bytecode = Compatibility12111Test.class
            .getClassLoader()
            .getResourceAsStream(resource)) {
            assertNotNull(bytecode, "Resolved Minecraft 1.21.11 bytecode is missing " + resource);
            ClassNode classNode = new ClassNode();
            new ClassReader(bytecode).accept(classNode, ClassReader.SKIP_FRAMES);
            MethodNode showMessage = classNode.methods.stream()
                .filter(method -> method.name.equals("showMessageToPlayer"))
                .findFirst()
                .orElseThrow();
            boolean targetExists = false;
            for (AbstractInsnNode instruction : showMessage.instructions) {
                if (instruction instanceof MethodInsnNode invoke
                    && invoke.owner.equals("net/minecraft/client/gui/components/ChatComponent")
                    && invoke.name.equals("addMessage")
                    && invoke.desc.equals(
                        "(Lnet/minecraft/network/chat/Component;"
                            + "Lnet/minecraft/network/chat/MessageSignature;"
                            + "Lnet/minecraft/client/GuiMessageTag;)V"
                    )) {
                    targetExists = true;
                    break;
                }
            }
            assertTrue(targetExists, "Minecraft 1.21.11 player chat Mixin target drifted");
        }
    }

    @Test
    void finalChatHooksMatchMinecraft12111() throws ReflectiveOperationException {
        assertEquals(void.class, ChatComponent.class.getDeclaredMethod(
            "addMessage",
            Component.class
        ).getReturnType());
        assertEquals(void.class, ChatComponent.class.getDeclaredMethod(
            "addMessage",
            Component.class,
            MessageSignature.class,
            GuiMessageTag.class
        ).getReturnType());
        assertEquals(
            "<\uE001 Villager_01> hello",
            BadgeComponents.insertBadgeBeforeExactlyOnePlayerChatHeaderName(
                Component.literal("<Villager_01> hello"),
                List.of("Villager_01")
            ).getString()
        );
    }

    @Test
    void finalServerTabTextCanBeDecoratedWithoutFlatteningIt() {
        Component original = Component.literal("ADMIN ")
            .append(Component.literal("Nattapat2871").withStyle(net.minecraft.ChatFormatting.RED));
        Component decorated = BadgeComponents.prependForExactlyOnePlayerName(
            original,
            List.of("Nattapat2871")
        );

        assertEquals("\uE001 ADMIN Nattapat2871", decorated.getString());
        assertEquals("ADMIN Nattapat2871", original.getString());
    }

    @Test
    void preservesF3LimiterDiagnosticsWhenBrandingPerformanceLine() {
        assertEquals(
            "60 fps T: 120 vsync · NamLauncher",
            BrandingFormatter.formatF3Performance("60 fps T: 120 vsync")
        );
    }
}
