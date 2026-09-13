// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

final class MinecraftMixinTargetTest {
    @Test
    void badgeConstructorHookIsStaticAndMatchesTheResourceManagerArguments() throws Exception {
        Class<?> manager = loadWithoutInitialization("net.minecraft.server.packs.resources.MultiPackResourceManager");
        Class<?> type = loadWithoutInitialization("net.minecraft.server.packs.PackType");
        assertNotNull(manager.getDeclaredConstructor(type, java.util.List.class));
        Method hook = loadWithoutInitialization("com.namlauncher.bridge.mixin.BadgeResourceManagerMixin")
            .getDeclaredMethod("namlauncher$protectBadge", java.util.List.class, type, java.util.List.class);
        assertTrue(java.lang.reflect.Modifier.isStatic(hook.getModifiers()), "Constructor HEAD must never use an uninitialized this");
    }

    private static Class<?> loadWithoutInitialization(String className) throws ClassNotFoundException {
        return Class.forName(
            className,
            false,
            MinecraftMixinTargetTest.class.getClassLoader()
        );
    }

    @Test
    void minecraft262StillExposesWindowTitleFactory() throws Exception {
        Class<?> minecraft = loadWithoutInitialization("net.minecraft.client.Minecraft");
        Method createTitle = minecraft.getDeclaredMethod("createTitle");
        assertEquals(String.class, createTitle.getReturnType());
        assertEquals(0, createTitle.getParameterCount());
    }

    @Test
    void minecraft262StillExposesVersionDebugEntryDisplay() throws Exception {
        Class<?> debugEntryVersion = loadWithoutInitialization(
            "net.minecraft.client.gui.components.debug.DebugEntryVersion"
        );
        assertTrue(
            Arrays.stream(debugEntryVersion.getDeclaredMethods()).anyMatch(method ->
                method.getName().equals("display")
                    && method.getParameterCount() == 4
                    && method.getReturnType() == void.class
            )
        );
    }

    @Test
    void minecraft262StillExposesFpsDebugEntryDisplay() throws Exception {
        Class<?> debugEntryFps = loadWithoutInitialization(
            "net.minecraft.client.gui.components.debug.DebugEntryFps"
        );
        assertTrue(
            Arrays.stream(debugEntryFps.getDeclaredMethods()).anyMatch(method ->
                method.getName().equals("display")
                    && method.getParameterCount() == 4
                    && method.getReturnType() == void.class
            )
        );
    }

    @Test
    void minecraft262StillExposesPlayerBadgeTargets() throws Exception {
        Class<?> playerInfo = loadWithoutInitialization("net.minecraft.client.multiplayer.PlayerInfo");
        Class<?> tabOverlay = loadWithoutInitialization(
            "net.minecraft.client.gui.components.PlayerTabOverlay"
        );
        Method displayName = tabOverlay.getDeclaredMethod("getNameForDisplay", playerInfo);
        assertEquals("net.minecraft.network.chat.Component", displayName.getReturnType().getName());

        Class<?> avatar = loadWithoutInitialization("net.minecraft.world.entity.Avatar");
        Class<?> avatarState = loadWithoutInitialization(
            "net.minecraft.client.renderer.entity.state.AvatarRenderState"
        );
        Class<?> avatarRenderer = loadWithoutInitialization(
            "net.minecraft.client.renderer.entity.player.AvatarRenderer"
        );
        Method extraction = avatarRenderer.getDeclaredMethod(
            "extractRenderState",
            avatar,
            avatarState,
            float.class
        );
        assertEquals(void.class, extraction.getReturnType());
    }

    @Test
    void minecraft262TabEntryConstructorHookStillMatchesBytecode() throws Exception {
        Class<?> component = loadWithoutInitialization("net.minecraft.network.chat.Component");
        Class<?> scoreDisplayEntry = loadWithoutInitialization(
            "net.minecraft.client.gui.components.PlayerTabOverlay$ScoreDisplayEntry"
        );
        assertNotNull(scoreDisplayEntry.getDeclaredConstructor(
            component,
            int.class,
            component,
            int.class
        ));

        String resource = "net/minecraft/client/gui/components/PlayerTabOverlay.class";
        try (InputStream bytecode = MinecraftMixinTargetTest.class
            .getClassLoader()
            .getResourceAsStream(resource)) {
            assertNotNull(bytecode, "Resolved Minecraft 26.2 bytecode is missing " + resource);

            ClassNode classNode = new ClassNode();
            new ClassReader(bytecode).accept(classNode, ClassReader.SKIP_FRAMES);
            MethodNode extraction = classNode.methods.stream()
                .filter(method -> method.name.equals("extractRenderState"))
                .findFirst()
                .orElseThrow();

            boolean targetInvokeExists = false;
            for (AbstractInsnNode instruction : extraction.instructions) {
                if (instruction instanceof MethodInsnNode invoke
                    && invoke.owner.equals(
                        "net/minecraft/client/gui/components/PlayerTabOverlay$ScoreDisplayEntry"
                    )
                    && invoke.name.equals("<init>")
                    && invoke.desc.equals(
                        "(Lnet/minecraft/network/chat/Component;I"
                            + "Lnet/minecraft/network/chat/Component;I)V"
                    )) {
                    targetInvokeExists = true;
                    break;
                }
            }
            assertTrue(targetInvokeExists, "Final Tab List entry constructor Mixin target drifted");
        }
    }

    @Test
    void minecraft262PlayerChatHookStillMatchesBytecode() throws Exception {
        String resource = "net/minecraft/client/multiplayer/chat/ChatListener.class";
        try (InputStream bytecode = MinecraftMixinTargetTest.class
            .getClassLoader()
            .getResourceAsStream(resource)) {
            assertNotNull(bytecode, "Resolved Minecraft 26.2 bytecode is missing " + resource);

            ClassNode classNode = new ClassNode();
            new ClassReader(bytecode).accept(classNode, ClassReader.SKIP_FRAMES);
            MethodNode showMessage = classNode.methods.stream()
                .filter(method -> method.name.equals("showMessageToPlayer"))
                .findFirst()
                .orElseThrow();

            boolean targetInvokeExists = false;
            for (AbstractInsnNode instruction : showMessage.instructions) {
                if (instruction instanceof MethodInsnNode invoke
                    && invoke.owner.equals("net/minecraft/client/gui/components/ChatComponent")
                    && invoke.name.equals("addPlayerMessage")
                    && invoke.desc.equals(
                        "(Lnet/minecraft/network/chat/Component;"
                            + "Lnet/minecraft/network/chat/MessageSignature;"
                            + "Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"
                    )) {
                    targetInvokeExists = true;
                    break;
                }
            }
            assertTrue(targetInvokeExists, "Player chat rendering Mixin target drifted");
        }
    }

    @Test
    void versionDebugEntryStillAddsItsHeaderAsAPriorityLine() throws Exception {
        String resource = "net/minecraft/client/gui/components/debug/DebugEntryVersion.class";
        try (InputStream bytecode = MinecraftMixinTargetTest.class
            .getClassLoader()
            .getResourceAsStream(resource)) {
            assertNotNull(bytecode, "Resolved Minecraft 26.2 bytecode is missing " + resource);

            ClassNode classNode = new ClassNode();
            new ClassReader(bytecode).accept(classNode, ClassReader.SKIP_FRAMES);
            MethodNode display = classNode.methods.stream()
                .filter(method -> method.name.equals("display"))
                .findFirst()
                .orElseThrow();

            boolean targetInvokeExists = false;
            for (AbstractInsnNode instruction : display.instructions) {
                if (instruction instanceof MethodInsnNode invoke
                    && invoke.owner.equals(
                        "net/minecraft/client/gui/components/debug/DebugScreenDisplayer"
                    )
                    && invoke.name.equals("addPriorityLine")
                    && invoke.desc.equals("(Ljava/lang/String;)V")) {
                    targetInvokeExists = true;
                    break;
                }
            }
            assertTrue(targetInvokeExists, "F3 branding Mixin invoke target drifted");
        }
    }

    @Test
    void fpsDebugEntryStillAddsItsDiagnosticAsAPriorityLine() throws Exception {
        String resource = "net/minecraft/client/gui/components/debug/DebugEntryFps.class";
        try (InputStream bytecode = MinecraftMixinTargetTest.class
            .getClassLoader()
            .getResourceAsStream(resource)) {
            assertNotNull(bytecode, "Resolved Minecraft 26.2 bytecode is missing " + resource);

            ClassNode classNode = new ClassNode();
            new ClassReader(bytecode).accept(classNode, ClassReader.SKIP_FRAMES);
            MethodNode display = classNode.methods.stream()
                .filter(method -> method.name.equals("display"))
                .findFirst()
                .orElseThrow();

            boolean targetInvokeExists = false;
            for (AbstractInsnNode instruction : display.instructions) {
                if (instruction instanceof MethodInsnNode invoke
                    && invoke.owner.equals("net/minecraft/client/gui/components/debug/DebugScreenDisplayer")
                    && invoke.name.equals("addPriorityLine")
                    && invoke.desc.equals("(Ljava/lang/String;)V")) {
                    targetInvokeExists = true;
                    break;
                }
            }
            assertTrue(targetInvokeExists, "F3 FPS branding Mixin invoke target drifted");
        }
    }
}
