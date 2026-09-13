// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.forge1201;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
final class CompatibilityTest {
    @Test void f3LinesRemainMutable() {
        net.minecraft.SharedConstants.tryDetectVersion();
        java.util.List<String> original = java.util.List.of("Minecraft 1.20.1 (1.20.1/forge)");
        java.util.List<String> result = BrandingFormatter.f3Lines(original);
        result.add("Targeted Block");
        assertEquals(2, result.size());
        assertEquals(1, original.size());
    }
    @Test void brandIsScopedToLauncher113() { assertEquals("NamLauncher v.1.1.16 (1.20.1)", BrandingFormatter.brandFor("1.20.1")); }
    @Test void glyphIsPrivateUseCodePoint() { assertEquals("\uE001", BadgeComponents.GLYPH); }
}
