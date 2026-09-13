// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.namlauncher.bridge.resources.BundledBadgeAssets;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import net.minecraft.util.IdentifierPattern;
import org.junit.jupiter.api.Test;

final class BadgeResourcePackTest {
    private static final Identifier FONT = Identifier.fromNamespaceAndPath("namlauncher", "font/badge.json");
    private static final Identifier IMAGE = Identifier.fromNamespaceAndPath("namlauncher", "textures/font/badge.png");
    private static final Identifier SERVER_FONT = Identifier.fromNamespaceAndPath("minecraft", "font/default.json");

    private static final class ServerPack implements PackResources {
        private final boolean override;
        ServerPack(boolean override) { this.override = override; }
        @Override public IoSupplier<InputStream> getRootResource(String... path) { return null; }
        @Override public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
            return id.equals(SERVER_FONT) || (override && (id.equals(FONT) || id.equals(IMAGE)))
                ? () -> new ByteArrayInputStream("server-owned".getBytes(StandardCharsets.UTF_8)) : null;
        }
        @Override public Set<String> getNamespaces(PackType type) { return Set.of("minecraft", "namlauncher"); }
        @Override public void listResources(PackType type, String namespace, String path, ResourceOutput output) { }
        @Override @SuppressWarnings("unchecked") public <T> T getMetadataSection(MetadataSectionType<T> type) {
            if (type != ResourceFilterSection.TYPE) return null;
            IdentifierPattern filter = IdentifierPattern.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"namespace\":\".*\",\"path\":\".*\"}")).getOrThrow();
            return (T) new ResourceFilterSection(List.of(filter));
        }
        @Override public PackLocationInfo location() {
            return new PackLocationInfo("server-test", Component.literal("Server pack"), PackSource.SERVER, Optional.empty());
        }
        @Override public void close() { }
    }

    @Test void badgeSurvivesNamespaceFiltersOverridesAndRepeatedReloads() throws Exception {
        for (boolean override : List.of(false, true)) {
            List<PackResources> serverPacks = List.of(new BadgeResourcePack(), new ServerPack(override));
            try (var unprotected = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, serverPacks)) {
                if (!override) assertTrue(unprotected.getResource(FONT).isEmpty(), "Fixture must really block the original badge");
            }
            for (int reload = 0; reload < 3; reload++) {
                try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                        BadgeResourcePack.protect(PackType.CLIENT_RESOURCES, serverPacks))) {
                    assertEquals(BundledBadgeAssets.PACK_ID, manager.getResource(FONT).orElseThrow().sourcePackId());
                    try (var stream = manager.getResource(FONT).orElseThrow().open()) {
                        var font = JsonParser.parseString(
                            new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                        ).getAsJsonObject();
                        var provider = font.getAsJsonArray("providers").get(0).getAsJsonObject();
                        assertEquals("namlauncher:font/badge.png", provider.get("file").getAsString());
                        assertEquals(8, provider.get("ascent").getAsInt());
                        assertEquals(8, provider.get("height").getAsInt());
                    }
                    try (var stream = manager.getResource(IMAGE).orElseThrow().open()) {
                        assertNotNull(ImageIO.read(stream), "Must serve the bundled PNG, not server bytes");
                    }
                    try (var stream = manager.getResource(SERVER_FONT).orElseThrow().open()) {
                        assertEquals("server-owned", new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                    }
                    assertTrue(manager.listResources("font", id -> id.equals(FONT)).containsKey(FONT));
                    assertEquals(1, manager.listPacks().filter(p -> p instanceof BadgeResourcePack).count());
                }
            }
            assertEquals(2, serverPacks.size(), "Never mutate the caller's pack list");
        }
    }

    @Test void packIsRestrictedToTwoClientAssetsAndLeavesDataPacksAlone() {
        var pack = new BadgeResourcePack();
        List<PackResources> original = List.of(pack);
        assertSame(original, BadgeResourcePack.protect(PackType.SERVER_DATA, original));
        assertNull(pack.getResource(PackType.SERVER_DATA, FONT));
        assertNull(pack.getResource(PackType.CLIENT_RESOURCES, SERVER_FONT));
        assertNull(pack.getResource(PackType.CLIENT_RESOURCES, Identifier.fromNamespaceAndPath("namlauncher", "private.txt")));
        assertNull(pack.getRootResource("namlauncher-internal", "badge.png"));
    }
}
