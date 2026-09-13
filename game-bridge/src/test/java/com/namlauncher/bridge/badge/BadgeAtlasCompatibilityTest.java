// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.*;
import com.namlauncher.bridge.resources.BundledBadgeAssets;
import java.lang.reflect.Proxy;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

final class BadgeAtlasCompatibilityTest {
    private Object insertIntoVanillaAtlas(int width, int height) throws Exception {
        Class<?> node = Class.forName("net.minecraft.client.gui.font.FontTexture$Node");
        Class<?> bitmap = Class.forName("com.mojang.blaze3d.font.GlyphBitmap");
        var constructor = node.getDeclaredConstructor(int.class, int.class, int.class, int.class);
        constructor.setAccessible(true);
        var insert = node.getDeclaredMethod("insert", bitmap);
        insert.setAccessible(true);
        Object glyph = Proxy.newProxyInstance(bitmap.getClassLoader(), new Class<?>[]{bitmap},
            (proxy, method, args) -> switch (method.getName()) {
                case "getPixelWidth" -> width;
                case "getPixelHeight" -> height;
                case "isColored" -> true;
                default -> throw new AssertionError("Unexpected GPU operation: " + method);
            });
        return insert.invoke(constructor.newInstance(0, 0, 256, 256), glyph);
    }

    @Test void bundledBadgeFitsWithoutImmediatelyFastAtlasResizing() throws Exception {
        assertNull(insertIntoVanillaAtlas(500, 500), "Reproduce the old full-size logo failure");
        for (int reload = 0; reload < 3; reload++) {
            try (var input = BundledBadgeAssets.open("textures/font/badge.png")) {
                var image = ImageIO.read(input);
                assertNotNull(image);
                assertEquals(BundledBadgeAssets.GLYPH_PIXELS, image.getWidth());
                assertEquals(BundledBadgeAssets.GLYPH_PIXELS, image.getHeight());
                assertEquals(256, image.getWidth(), "Keep the protected glyph within the vanilla atlas limit");
                assertNotNull(insertIntoVanillaAtlas(image.getWidth(), image.getHeight()),
                    "Badge must fit the real vanilla allocator with server packs enabled");
                assertTrue(image.getColorModel().hasAlpha());
            }
        }
    }

    @Test void bundledBadgeUsesTheWholeGlyphWithoutClippingItsBottom() throws Exception {
        try (var input = BundledBadgeAssets.open("textures/font/badge.png")) {
            var image = ImageIO.read(input);
            int minX = image.getWidth(), minY = image.getHeight(), maxX = -1, maxY = -1;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) <= 8) continue;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
            assertTrue(minX <= 3 && minY <= 3, "Only a narrow visual safety margin is expected");
            assertTrue(maxX >= 251 && maxY >= 253, "The high-resolution glyph must use the full safe canvas");
            assertEquals(0, image.getRGB(0, 0) >>> 24, "The safety margin must stay transparent");
        }
    }
}
