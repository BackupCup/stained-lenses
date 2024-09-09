package net.backupcup.stainedlenses.utils

import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import java.util.regex.MatchResult
import java.util.regex.Pattern


object TextWrapUtils {
    private fun generate(screenWidth: Int): Pattern {
        return Pattern.compile(String.format("(\\S.{1,%d})(?:\\s+|$)", Width.getLineWidth(screenWidth)))
    }

    fun wrapText(screenWidth: Int, translationKey: String?, formatting: Formatting?): List<Text> {
        return generate(screenWidth).matcher(Text.translatable(translationKey).string)
            .results().map { res: MatchResult ->
                Text.literal(
                    res.group(1)
                ).formatted(formatting) as Text
            }
            .toList()
    }

    fun wrapText(screenWidth: Int, translationKey: String?, vararg formatting: Formatting?): List<Text> {
        return generate(screenWidth).matcher(Text.translatable(translationKey).string)
            .results().map { res: MatchResult ->
                Text.literal(
                    res.group(1)
                ).formatted(*formatting) as Text
            }
            .toList()
    }

    fun wrapText(screenWidth: Int, text: Text, vararg formatting: Formatting?): List<Text> {
        return generate(screenWidth).matcher(text.string)
            .results().map { res: MatchResult ->
                Text.literal(
                    res.group(1)
                ).formatted(*formatting) as Text
            }
            .toList()
    }

    private enum class Width(private val screen: Int, val line: Int) {
        SMALL(320, 20),
        NORMAL(450, 35),
        WIDE(590, 50),
        ULTRAWIDE(750, 65);

        companion object {
            fun getLineWidth(screenWidth: Int): Int {
                return Arrays.stream(entries.toTypedArray())
                    .reduce { current: Width?, next: Width -> if (next.screen <= screenWidth) next else current }
                    .map { obj: Width -> obj.line }.orElse(SMALL.line)
            }
        }
    }
}
