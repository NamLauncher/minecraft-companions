// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

final class CompatibilityTest {
    @Test
    void f3ResultCanBeExtendedByVanilla() {
        // Plain JUnit has no Fabric game container; use the formatter's detected value.
        String version = BrandingFormatter.BRAND.substring(BrandingFormatter.BRAND.indexOf('(') + 1).replace(")", "");
        java.util.List<String> original = java.util.List.of("Minecraft " + version + " (fabric)");
        java.util.List<String> result = BrandingFormatter.f3Lines(original);
        result.add("Targeted Block");
        assertEquals(2, result.size());
        assertEquals(1, original.size());
    }

    private static Class<?> load(String name) throws ClassNotFoundException {
        return Class.forName(name, false, CompatibilityTest.class.getClassLoader());
    }

    @Test
    void minecraft1201ExposesTheExactTabAndNametagHooks() throws Exception {
        Class<?> info = load("net.minecraft.client.multiplayer.PlayerInfo");
        Method tabName = load("net.minecraft.client.gui.components.PlayerTabOverlay")
            .getDeclaredMethod("getNameForDisplay", info);
        assertEquals(Component.class, tabName.getReturnType());

        Class<?> player = load("net.minecraft.client.player.AbstractClientPlayer");
        Class<?> pose = load("com.mojang.blaze3d.vertex.PoseStack");
        Class<?> buffers = load("net.minecraft.client.renderer.MultiBufferSource");
        Method nameTag = load("net.minecraft.client.renderer.entity.player.PlayerRenderer")
            .getDeclaredMethod("renderNameTag", player, Component.class, pose, buffers, int.class);
        assertEquals(void.class, nameTag.getReturnType());
    }

    @Test
    void playerRendererStillDelegatesToTheMixinModifyArgTarget() throws Exception {
        String resource = "net/minecraft/client/renderer/entity/player/PlayerRenderer.class";
        try (InputStream bytecode = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(bytecode);
            ClassNode node = new ClassNode();
            new ClassReader(bytecode).accept(node, ClassReader.SKIP_FRAMES);
            MethodNode method = node.methods.stream()
                .filter(candidate -> candidate.name.equals("renderNameTag"))
                .filter(candidate -> candidate.desc.endsWith(";I)V"))
                .findFirst()
                .orElseThrow();
            boolean found = false;
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode invoke
                    && invoke.owner.equals("net/minecraft/client/renderer/entity/LivingEntityRenderer")
                    && invoke.name.equals("renderNameTag")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "PlayerRenderer nametag delegation drifted");
        }
    }

    @Test
    void badgePreservesTheFullFormattedName() {
        Component original = Component.literal("[Team] Nattapat2871");
        assertEquals("\uE001 [Team] Nattapat2871", BadgeComponents.prepend(original).getString());
    }
}
