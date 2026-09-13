// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import com.namlauncher.bridge.resources.BundledBadgeAssets;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;

/** Last-priority client pack protects the badge from server pack namespace filters. */
public final class BadgeResourcePack implements PackResources {
    public static List<PackResources> protect(PackType type, List<PackResources> original) {
        if (type != PackType.CLIENT_RESOURCES) return original;
        ArrayList<PackResources> packs = new ArrayList<>(original.size() + 1);
        for (PackResources pack : original) {
            if (!(pack instanceof BadgeResourcePack)) packs.add(pack);
        }
        packs.add(new BadgeResourcePack());
        return packs;
    }

    @Override public IoSupplier<InputStream> getRootResource(String... path) { return null; }
    @Override public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        if (type != PackType.CLIENT_RESOURCES || !BundledBadgeAssets.NAMESPACE.equals(id.getNamespace())
            || !BundledBadgeAssets.PATHS.contains(id.getPath())) return null;
        return () -> BundledBadgeAssets.open(id.getPath());
    }
    @Override public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !BundledBadgeAssets.NAMESPACE.equals(namespace)) return;
        String prefix = path.isEmpty() ? "" : path.endsWith("/") ? path : path + "/";
        for (String resource : BundledBadgeAssets.PATHS) {
            if (resource.startsWith(prefix)) {
                output.accept(Identifier.fromNamespaceAndPath(namespace, resource), () -> BundledBadgeAssets.open(resource));
            }
        }
    }
    @Override public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(BundledBadgeAssets.NAMESPACE) : Set.of();
    }
    @Override public <T> T getMetadataSection(MetadataSectionType<T> metadata) { return null; }
    @Override public PackLocationInfo location() {
        return new PackLocationInfo(BundledBadgeAssets.PACK_ID, Component.literal("NamLauncher badge"), PackSource.BUILT_IN, Optional.empty());
    }
    @Override public void close() { }
}
