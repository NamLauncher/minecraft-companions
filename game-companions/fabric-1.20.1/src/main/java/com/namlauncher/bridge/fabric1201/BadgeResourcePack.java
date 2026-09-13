// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import com.namlauncher.bridge.resources.BundledBadgeAssets;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
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
    @Override public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        if (type != PackType.CLIENT_RESOURCES || !BundledBadgeAssets.NAMESPACE.equals(id.getNamespace())
            || !BundledBadgeAssets.PATHS.contains(id.getPath())) return null;
        return () -> BundledBadgeAssets.open(id.getPath());
    }
    @Override public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !BundledBadgeAssets.NAMESPACE.equals(namespace)) return;
        String prefix = path.isEmpty() ? "" : path.endsWith("/") ? path : path + "/";
        for (String resource : BundledBadgeAssets.PATHS) {
            if (resource.startsWith(prefix)) {
                output.accept(new ResourceLocation(namespace, resource), () -> BundledBadgeAssets.open(resource));
            }
        }
    }
    @Override public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(BundledBadgeAssets.NAMESPACE) : Set.of();
    }
    @Override public <T> T getMetadataSection(MetadataSectionSerializer<T> metadata) { return null; }
    @Override public String packId() { return BundledBadgeAssets.PACK_ID; }
    @Override public boolean isBuiltin() { return true; }
    @Override public void close() { }
}
