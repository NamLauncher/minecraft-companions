// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/** Only our two badge assets; never a replacement for the server's resource pack. */
public final class BundledBadgeAssets {
    // Vanilla font atlases are 256x256. The launcher icon (500x500) is not a glyph.
    // Do not depend on ImmediatelyFast enlarging the atlas: server shaders disable it.
    public static final int GLYPH_PIXELS = 256;
    private static final int GLYPH_PADDING = 2;
    private static final int ALPHA_THRESHOLD = 8;
    private static byte[] glyphPng;
    public static final String NAMESPACE = "namlauncher";
    public static final String PACK_ID = "namlauncher:protected-badge";
    public static final Set<String> PATHS = Set.of("font/badge.json", "textures/font/badge.png");
    private static final byte[] FONT = (
        "{\"providers\":[{\"type\":\"bitmap\",\"file\":\"namlauncher:font/badge.png\","
        + "\"ascent\":8,\"height\":8,\"chars\":[\"\\uE001\"]}]}"
    ).getBytes(StandardCharsets.UTF_8);

    private BundledBadgeAssets() { }

    public static InputStream open(String path) throws IOException {
        if ("font/badge.json".equals(path)) return new ByteArrayInputStream(FONT);
        if (!"textures/font/badge.png".equals(path)) throw new IOException("Unknown badge resource");
        // Private classpath entry cannot be replaced by a Minecraft resource pack.
        return new ByteArrayInputStream(glyphBytes());
    }

    private static synchronized byte[] glyphBytes() throws IOException {
        if (glyphPng != null) return glyphPng;
        try (InputStream stream = BundledBadgeAssets.class.getResourceAsStream("/namlauncher-internal/badge.png")) {
            if (stream == null) throw new IOException("Bundled NamLauncher badge is missing");
            try (var input = new MemoryCacheImageInputStream(stream)) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IOException("Bundled NamLauncher badge is not a PNG");
                var reader = readers.next();
                BufferedImage source;
                try {
                    reader.setInput(input);
                    source = reader.read(0);
                } finally {
                    reader.dispose();
                }
                int minX = source.getWidth();
                int minY = source.getHeight();
                int maxX = -1;
                int maxY = -1;
                for (int y = 0; y < source.getHeight(); y++) {
                    for (int x = 0; x < source.getWidth(); x++) {
                        if ((source.getRGB(x, y) >>> 24) <= ALPHA_THRESHOLD) continue;
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
                if (maxX < minX || maxY < minY) {
                    minX = 0;
                    minY = 0;
                    maxX = source.getWidth() - 1;
                    maxY = source.getHeight() - 1;
                }
                int contentWidth = maxX - minX + 1;
                int contentHeight = maxY - minY + 1;
                int available = GLYPH_PIXELS - GLYPH_PADDING * 2;
                double scale = Math.min((double) available / contentWidth, (double) available / contentHeight);
                int targetWidth = Math.max(1, (int) Math.round(contentWidth * scale));
                int targetHeight = Math.max(1, (int) Math.round(contentHeight * scale));
                int targetX = (GLYPH_PIXELS - targetWidth) / 2;
                int targetY = (GLYPH_PIXELS - targetHeight) / 2;
                BufferedImage glyph = new BufferedImage(GLYPH_PIXELS, GLYPH_PIXELS, BufferedImage.TYPE_INT_ARGB);
                var graphics = glyph.createGraphics();
                try {
                    graphics.setComposite(AlphaComposite.Src);
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    graphics.drawImage(source, targetX, targetY, targetX + targetWidth, targetY + targetHeight,
                        minX, minY, maxX + 1, maxY + 1, null);
                } finally {
                    graphics.dispose();
                    source.flush();
                }
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (var output = new MemoryCacheImageOutputStream(bytes)) {
                    if (!ImageIO.write(glyph, "PNG", output)) throw new IOException("PNG writer unavailable");
                } finally {
                    glyph.flush();
                }
                glyphPng = bytes.toByteArray();
                return glyphPng;
            }
        }
    }
}
