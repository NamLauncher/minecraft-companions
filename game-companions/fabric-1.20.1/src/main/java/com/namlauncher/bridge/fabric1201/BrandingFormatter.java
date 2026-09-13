// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

public final class BrandingFormatter {
    private static final String MINECRAFT_VERSION = FabricLoader.getInstance()
        .getModContainer("minecraft")
        .map(container -> container.getMetadata().getVersion().getFriendlyString())
        .orElse("unknown");
    public static final String BRAND = "NamLauncher v.1.1.16 (" + MINECRAFT_VERSION + ")";
    private static final Pattern TITLE = Pattern.compile(
        "^Minecraft\\*?\\s+" + Pattern.quote(MINECRAFT_VERSION) + "(?:\\s+-\\s+(.*))?$"
    );
    private static final Pattern F3 = Pattern.compile(
        "^Minecraft\\*?\\s+" + Pattern.quote(MINECRAFT_VERSION) + "(.*)$"
    );

    private BrandingFormatter() {
    }

    public static String windowTitle(String original) {
        if (original == null || original.startsWith(BRAND)) return original;
        Matcher matcher = TITLE.matcher(original.strip());
        if (!matcher.matches()) return original;
        String context = matcher.group(1);
        return context == null || context.isBlank() ? BRAND : BRAND + " - " + context.strip();
    }

    public static List<String> f3Lines(List<String> original) {
        if (original == null || original.isEmpty()) return original;
        Matcher matcher = F3.matcher(original.get(0));
        if (!matcher.matches()) return original;
        ArrayList<String> branded = new ArrayList<>(original);
        branded.set(0, BRAND + matcher.group(1));
        // Vanilla and other debug overlays append diagnostics after this hook.
        return branded;
    }
}
