// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public final class BrandingFormatter {
    private static final Pattern TITLE = Pattern.compile("^Minecraft\\*?\\s+1\\.21\\.11(?:\\s+-\\s+(.*))?$");
    private static final Pattern F3 = Pattern.compile("^Minecraft\\*?\\s+1\\.21\\.11(.*)$");
    private static final Pattern F3_PERFORMANCE = Pattern.compile("^\\d+ fps T: (?:inf|\\d+)(?: \\S.*)?$");
    private static final String F3_PERFORMANCE_BRAND = " · NamLauncher";
    private BrandingFormatter() { }
    public static String formatWindowTitle(String original) {
        if (original == null || original.isBlank() || original.startsWith(BrandingConstants.BRAND)) return original;
        Matcher matcher = TITLE.matcher(original.strip());
        if (!matcher.matches()) return original;
        String context = matcher.group(1);
        return context == null || context.isBlank() ? BrandingConstants.BRAND : BrandingConstants.BRAND + " - " + context.strip();
    }
    public static String formatF3Header(String original) {
        if (original == null || original.isBlank() || original.startsWith(BrandingConstants.BRAND)) return original;
        Matcher matcher = F3.matcher(original);
        return matcher.matches() ? BrandingConstants.BRAND + matcher.group(1) : original;
    }
    /** Preserve the real frame limiter/vsync diagnostic and append our brand once. */
    public static String formatF3Performance(String original) {
        if (original == null || original.endsWith(F3_PERFORMANCE_BRAND)
            || !F3_PERFORMANCE.matcher(original).matches()) return original;
        return original + F3_PERFORMANCE_BRAND;
    }
    public static List<String> formatF3Lines(List<String> original) {
        if (original == null || original.isEmpty()) return original;
        String first = original.getFirst(); String branded = formatF3Header(first);
        if (Objects.equals(first, branded)) return original;
        // Keep the result mutable: vanilla appends diagnostics after this hook.
        ArrayList<String> copy = new ArrayList<>(original); copy.set(0, branded); return copy;
    }
}
