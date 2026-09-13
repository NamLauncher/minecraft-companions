// Author/creator: nattapat2871 (https://nattapat2871.me)
package com.namlauncher.bridge.badge;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class BadgeComponents {
    public static final String GLYPH = "\uE001";
    private static final Pattern PLAYER_NAME_TOKEN = Pattern.compile(
        "(?<![A-Za-z0-9_])[A-Za-z0-9_]{1,16}(?![A-Za-z0-9_])"
    );
    private static final String CHAT_HEADER_SEPARATORS = ":：>»›|•";
    private static final int MAX_SEPARATOR_DISTANCE = 32;
    private static final FontDescription.Resource BADGE_FONT = new FontDescription.Resource(
        Identifier.fromNamespaceAndPath("namlauncher", "badge")
    );
    private static final Style BADGE_STYLE = Style.EMPTY.withFont(BADGE_FONT);

    private BadgeComponents() {
    }

    /** Check one exact player-name token without flattening or rewriting server styling. */
    public static boolean containsExactlyOnePlayerName(Component original, String playerName) {
        if (original == null || playerName == null || !playerName.matches("[A-Za-z0-9_]{1,16}")) return false;
        var matcher = java.util.regex.Pattern.compile("(?<![A-Za-z0-9_])"
            + java.util.regex.Pattern.quote(playerName) + "(?![A-Za-z0-9_])").matcher(original.getString());
        return matcher.find() && !matcher.find();
    }

    /** Decorate only when the final server-owned text identifies one known player. */
    public static Component prependForExactlyOnePlayerName(
        Component original,
        Collection<String> playerNames
    ) {
        if (original == null || playerNames == null || playerNames.isEmpty()) return original;
        String matchedName = null;
        var matcher = PLAYER_NAME_TOKEN.matcher(original.getString());
        while (matcher.find()) {
            String playerName = matcher.group();
            if (!playerNames.contains(playerName)) continue;
            if (matchedName != null) return original;
            matchedName = playerName;
        }
        return matchedName == null ? original : prepend(original);
    }

    /**
     * Decorate server/plugin chat only when one active player name appears in
     * the first-line header and is followed by a conventional chat separator.
     */
    public static Component insertBadgeBeforeExactlyOnePlayerChatHeaderName(
        Component original,
        Collection<String> playerNames
    ) {
        if (original == null || playerNames == null || playerNames.isEmpty() || isDecorated(original)) {
            return original;
        }
        String plainText = original.getString();
        int lineBreak = plainText.indexOf('\n');
        String firstLine = lineBreak < 0 ? plainText : plainText.substring(0, lineBreak);
        int matchedNameStart = -1;
        var matcher = PLAYER_NAME_TOKEN.matcher(firstLine);
        while (matcher.find()) {
            String playerName = matcher.group();
            if (!playerNames.contains(playerName)
                || !hasNearbyChatSeparator(firstLine, matcher.end())) continue;
            if (matchedNameStart >= 0) return original;
            matchedNameStart = matcher.start();
        }
        return matchedNameStart < 0 ? original : insertBadgeBeforeTextIndex(original, matchedNameStart);
    }

    /** Place the badge immediately before one verified sender name in the chat header. */
    public static Component insertBadgeBeforePlayerChatName(Component original, String playerName) {
        if (original == null || playerName == null || !playerName.matches("[A-Za-z0-9_]{1,16}")) {
            return original;
        }
        return insertBadgeBeforeExactlyOnePlayerChatHeaderName(original, java.util.Set.of(playerName));
    }

    private static boolean hasNearbyChatSeparator(String line, int playerNameEnd) {
        int end = Math.min(line.length(), playerNameEnd + MAX_SEPARATOR_DISTANCE + 1);
        for (int index = playerNameEnd; index < end; index++) {
            char value = line.charAt(index);
            if (CHAT_HEADER_SEPARATORS.indexOf(value) >= 0) return true;
            if (Character.isLetterOrDigit(value) || value == '_') return false;
        }
        return false;
    }

    public static boolean isDecorated(Component original) {
        if (original == null) return false;
        // Servers may use the same private-use character for their own rank icons.
        return original.visit((textStyle, text) -> text.isEmpty()
            ? Optional.<Boolean>empty()
            : text.contains(GLYPH) && BADGE_STYLE.getFont().equals(textStyle.getFont())
                ? Optional.of(true)
                : Optional.empty(),
            Style.EMPTY).orElse(false);
    }

    private static Component insertBadgeBeforeTextIndex(Component original, int insertionIndex) {
        MutableComponent decorated = Component.empty();
        int[] plainOffset = { 0 };
        boolean[] inserted = { false };
        original.visit((textStyle, text) -> {
            int segmentStart = plainOffset[0];
            int segmentEnd = segmentStart + text.length();
            if (!inserted[0] && insertionIndex >= segmentStart && insertionIndex <= segmentEnd) {
                int localIndex = insertionIndex - segmentStart;
                appendStyledText(decorated, text.substring(0, localIndex), textStyle);
                decorated.append(Component.literal(GLYPH).withStyle(BADGE_STYLE));
                decorated.append(Component.literal(" "));
                appendStyledText(decorated, text.substring(localIndex), textStyle);
                inserted[0] = true;
            } else {
                appendStyledText(decorated, text, textStyle);
            }
            plainOffset[0] = segmentEnd;
            return Optional.empty();
        }, Style.EMPTY);
        return inserted[0] ? decorated : original;
    }

    private static void appendStyledText(MutableComponent target, String text, Style style) {
        if (!text.isEmpty()) target.append(Component.literal(text).withStyle(style));
    }

    public static Component prepend(Component original) {
        if (original == null || isDecorated(original)) return original;
        return Component.empty()
            .append(Component.literal(GLYPH).withStyle(BADGE_STYLE))
            .append(Component.literal(" "))
            .append(original.copy());
    }
}
