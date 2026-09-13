// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

final class Compatibility1211Test {
    private static Class<?> load(String name) throws ClassNotFoundException {
        return Class.forName(name, false, Compatibility1211Test.class.getClassLoader());
    }

    @Test
    void minecraft1211ExposesTheExactBadgeHooks() throws Exception {
        Class<?> info = load("net.minecraft.client.multiplayer.PlayerInfo");
        Method tabName = load("net.minecraft.client.gui.components.PlayerTabOverlay")
            .getDeclaredMethod("getNameForDisplay", info);
        assertEquals(Component.class, tabName.getReturnType());

        Method nameTag = load("net.minecraft.client.renderer.entity.player.PlayerRenderer")
            .getDeclaredMethod(
                "renderNameTag",
                load("net.minecraft.client.player.AbstractClientPlayer"),
                Component.class,
                load("com.mojang.blaze3d.vertex.PoseStack"),
                load("net.minecraft.client.renderer.MultiBufferSource"),
                int.class,
                float.class
            );
        assertEquals(void.class, nameTag.getReturnType());
    }
}
