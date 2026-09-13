// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;

public final class BrandingFormatter {
    private BrandingFormatter() { }
    public static String brand() { return brandFor(SharedConstants.getCurrentVersion().getName()); }
    static String brandFor(String minecraftVersion) { return "NamLauncher v.1.1.16 (" + minecraftVersion + ")"; }
    public static String windowTitle(String original) {
        String minecraftVersion = SharedConstants.getCurrentVersion().getName();
        String brand = brandFor(minecraftVersion);
        if (original == null || original.startsWith(brand)) return original;
        Matcher matcher = Pattern.compile("^Minecraft\\*?\\s+" + Pattern.quote(minecraftVersion) + "(?:\\s+-\\s+(.*))?$").matcher(original.strip());
        if (!matcher.matches()) return original;
        String context = matcher.group(1);
        return context == null || context.isBlank() ? brand : brand + " - " + context.strip();
    }
    public static List<String> f3Lines(List<String> original) {
        if (original == null || original.isEmpty()) return original;
        String minecraftVersion = SharedConstants.getCurrentVersion().getName();
        Matcher matcher = Pattern.compile("^Minecraft\\*?\\s+" + Pattern.quote(minecraftVersion) + "(.*)$").matcher(original.get(0));
        if (!matcher.matches()) return original;
        ArrayList<String> branded = new ArrayList<>(original);
        branded.set(0, brandFor(minecraftVersion) + matcher.group(1));
        // Vanilla and other debug overlays append diagnostics after this hook.
        return branded;
    }
}
