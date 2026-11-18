package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankMarkTest {

  @ParameterizedTest(name = "{0} in {1}")
  @CsvSource({"◇, blue, BLUE", "✠, dark_aqua, DARK_AQUA", "◆, #3355ff,"})
  @DisplayName("a mark is a shape in a colour, and draws as that shape in that colour")
  void drawsItsShape(final String glyph, final String color, final Formatting named) {
    final RankMark mark =
        RankMark.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"glyph\":\"" + glyph + "\",\"color\":\"" + color + "\"}"))
            .getOrThrow();
    final Text drawn = mark.text();
    assertEquals(glyph, drawn.getString());
    assertEquals(
        named == null ? TextColor.parse(color).getOrThrow() : TextColor.fromFormatting(named),
        drawn.getStyle().getColor());
  }

  @ParameterizedTest(name = "[{0}]")
  @CsvSource({"''", "ABCDE"})
  @DisplayName("a mark must be a short shape, never empty or a word")
  void refusesBadGlyphs(final String glyph) {
    assertTrue(
        RankMark.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"glyph\":\"" + glyph + "\",\"color\":\"blue\"}"))
            .isError());
  }
}
