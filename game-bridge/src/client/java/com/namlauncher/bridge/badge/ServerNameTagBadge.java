// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

/** Only decorate a visible label anchored above one locally present, badged player. */
public final class ServerNameTagBadge {
    private static final long CACHE_TICKS = 40;
    private static final Map<Entity, CachedDecoration> CACHE = new WeakHashMap<>();

    private ServerNameTagBadge() { }

    public static Component decorate(Entity label, Component text) {
        var minecraft = Minecraft.getInstance();
        if (text == null || minecraft.level == null) return text;
        Set<UUID> activeBadges = BadgeRegistry.snapshot();
        long gameTime = minecraft.level.getGameTime();
        CachedDecoration cached = CACHE.get(label);
        if (cached != null
            && cached.activeBadges() == activeBadges
            && cached.original().equals(text)
            && gameTime >= cached.checkedAt()
            && gameTime < cached.checkedAt() + CACHE_TICKS) {
            return cached.decorated();
        }

        if (text.getString().length() > 256) {
            CACHE.put(label, new CachedDecoration(text, activeBadges, gameTime, text));
            return text;
        }

        var nearbyBadgedNames = new ArrayList<String>(2);
        for (UUID playerUuid : activeBadges) {
            var player = minecraft.level.getPlayerByUUID(playerUuid);
            if (player == null || player.isInvisible()) continue;
            double dx = label.getX() - player.getX(), dz = label.getZ() - player.getZ();
            double dy = label.getY() - player.getY();
            if (dx * dx + dz * dz > 0.64 || dy < 0 || dy > 4) continue;
            nearbyBadgedNames.add(player.getName().getString());
        }
        Component decorated = BadgeComponents.prependForExactlyOnePlayerName(text, nearbyBadgedNames);
        CACHE.put(label, new CachedDecoration(text, activeBadges, gameTime, decorated));
        return decorated;
    }

    private record CachedDecoration(
        Component original,
        Set<UUID> activeBadges,
        long checkedAt,
        Component decorated
    ) { }
}
