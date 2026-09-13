// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class BadgeComponents {
    public static final String GLYPH = "\uE001";
    private static final Style STYLE = Style.EMPTY.withFont(
        ResourceLocation.fromNamespaceAndPath("namlauncher", "badge")
    );

    private BadgeComponents() {
    }

    public static Component prepend(Component original) {
        if (original == null) return null;
        // Servers may use the same private-use character for their own rank icons.
        boolean alreadyDecorated = original.visit((textStyle, text) -> text.isEmpty()
            ? java.util.Optional.<Boolean>empty()
            : java.util.Optional.of(text.startsWith(GLYPH) && STYLE.getFont().equals(textStyle.getFont())),
            Style.EMPTY).orElse(false);
        if (alreadyDecorated) return original;
        return Component.empty()
            .append(Component.literal(GLYPH).withStyle(STYLE))
            .append(Component.literal(" "))
            .append(original.copy());
    }
}
