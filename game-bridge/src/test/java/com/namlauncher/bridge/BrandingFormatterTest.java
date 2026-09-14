// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.Test;

final class BrandingFormatterTest {
    @Test
    void formatsSingleplayerWindowTitle() {
        assertEquals(
            "NamLauncher v.1.2.4 (26.2) - Singleplayer",
            BrandingFormatter.formatWindowTitle("Minecraft 26.2 - Singleplayer")
        );
    }

    @Test
    void formatsThirdPartyServerWindowTitle() {
        assertEquals(
            "NamLauncher v.1.2.4 (26.2) - Multiplayer (3rd-party Server)",
            BrandingFormatter.formatWindowTitle("Minecraft* 26.2 - Multiplayer (3rd-party Server)")
        );
    }

    @Test
    void leavesUnknownOrThirdPartyTitleUntouched() {
        String title = "A performance mod supplied this title";
        assertSame(title, BrandingFormatter.formatWindowTitle(title));
        assertNull(BrandingFormatter.formatWindowTitle(null));
    }

    @Test
    void preservesF3DiagnosticSuffix() {
        assertEquals(
            "NamLauncher v.1.2.4 (26.2) (26.2/fabric)",
            BrandingFormatter.formatF3Header("Minecraft 26.2 (26.2/fabric)")
        );
    }

    @Test
    void appendsBrandWithoutReplacingPresentModeOrFrameLimit() {
        assertEquals(
            "172 fps T: inf (fifo) · NamLauncher",
            BrandingFormatter.formatF3Performance("172 fps T: inf (fifo)")
        );
        assertEquals(
            "60 fps T: 120 vsync · NamLauncher",
            BrandingFormatter.formatF3Performance("60 fps T: 120 vsync")
        );
        String branded = "172 fps T: inf (fifo) · NamLauncher";
        assertSame(branded, BrandingFormatter.formatF3Performance(branded));
        String thirdParty = "Sodium Renderer (0.9.1)";
        assertSame(thirdParty, BrandingFormatter.formatF3Performance(thirdParty));
    }

    @Test
    void changesOnlyTheFirstF3Line() {
        List<String> original = List.of(
            "Minecraft 26.2 (26.2/fabric)",
            "60 fps T: 120",
            "Integrated server @ 20 ms ticks"
        );
        List<String> branded = BrandingFormatter.formatF3Lines(original);

        assertNotSame(original, branded);
        assertEquals("NamLauncher v.1.2.4 (26.2) (26.2/fabric)", branded.getFirst());
        assertEquals(original.subList(1, original.size()), branded.subList(1, branded.size()));
    }

    @Test
    void permitsVanillaToAppendDiagnosticsAfterBranding() {
        List<String> original = List.of("Minecraft 26.2 (26.2/fabric)");
        List<String> branded = BrandingFormatter.formatF3Lines(original);
        branded.add("Targeted Block: 0, 64, 0");
        branded.set(1, "Targeted Entity: minecraft:player");
        assertEquals(2, branded.size());
        assertEquals(1, original.size());
    }

    @Test
    void returnsOriginalListWhenHeaderShapeIsUnknown() {
        List<String> original = List.of("Unknown debug header", "diagnostic line");
        assertSame(original, BrandingFormatter.formatF3Lines(original));
    }
}
