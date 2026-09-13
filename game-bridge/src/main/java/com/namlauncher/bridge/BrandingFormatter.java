// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BrandingFormatter {
    private static final Pattern MINECRAFT_TITLE = Pattern.compile(
        "^Minecraft\\*?\\s+26\\.2(?:\\s+-\\s+(.*))?$"
    );
    private static final Pattern MINECRAFT_F3_HEADER = Pattern.compile(
        "^Minecraft\\*?\\s+26\\.2(.*)$"
    );
    private static final Pattern MINECRAFT_F3_PERFORMANCE = Pattern.compile(
        "^\\d+ fps T: (?:inf|\\d+)(?: \\S.*)?$"
    );
    private static final String F3_PERFORMANCE_BRAND = " · NamLauncher";

    private BrandingFormatter() {
    }

    /**
     * Rebrands only the vanilla 26.2 title shape. Unknown or third-party title
     * formats are returned untouched so a mapping or compatibility drift cannot
     * destroy information supplied by another mod.
     */
    public static String formatWindowTitle(String original) {
        if (original == null || original.isBlank() || original.startsWith(BrandingConstants.BRAND)) {
            return original;
        }

        Matcher matcher = MINECRAFT_TITLE.matcher(original.strip());
        if (!matcher.matches()) {
            return original;
        }

        String context = matcher.group(1);
        return context == null || context.isBlank()
            ? BrandingConstants.BRAND
            : BrandingConstants.BRAND + " - " + context.strip();
    }

    /**
     * Replaces only the leading Minecraft brand/version in the F3 header. The
     * loader, renderer, server, and other diagnostic suffixes remain byte-for-byte
     * unchanged.
     */
    public static String formatF3Header(String original) {
        if (original == null || original.isBlank() || original.startsWith(BrandingConstants.BRAND)) {
            return original;
        }

        Matcher matcher = MINECRAFT_F3_HEADER.matcher(original);
        if (!matcher.matches()) {
            return original;
        }
        return BrandingConstants.BRAND + matcher.group(1);
    }

    /** Preserve the real frame limiter/present-mode diagnostic and append our brand once. */
    public static String formatF3Performance(String original) {
        if (original == null || original.endsWith(F3_PERFORMANCE_BRAND)
            || !MINECRAFT_F3_PERFORMANCE.matcher(original).matches()) {
            return original;
        }
        return original + F3_PERFORMANCE_BRAND;
    }

    public static List<String> formatF3Lines(List<String> original) {
        if (original == null || original.isEmpty()) {
            return original;
        }

        String firstLine = original.getFirst();
        String brandedFirstLine = formatF3Header(firstLine);
        if (Objects.equals(firstLine, brandedFirstLine)) {
            return original;
        }

        ArrayList<String> branded = new ArrayList<>(original);
        branded.set(0, brandedFirstLine);
        // Vanilla and other debug overlays append diagnostics after this hook.
        return branded;
    }
}
