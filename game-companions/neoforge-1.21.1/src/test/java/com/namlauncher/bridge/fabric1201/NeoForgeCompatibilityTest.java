// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.fabric1201;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
final class NeoForgeCompatibilityTest {
    @Test void brandIsScopedToLauncher113() { assertEquals("NamLauncher v.1.1.16 (1.21.1)", BrandingFormatter.brandFor("1.21.1")); }
    @Test void glyphIsPrivateUseCodePoint() { assertEquals("\uE001", BadgeComponents.GLYPH); }
}
