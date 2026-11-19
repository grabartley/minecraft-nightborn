package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RankMarkTest {

  @ParameterizedTest(name = "{0} in {1}")
  @CsvSource({"◇, blue, BLUE", "✠, dark_aqua, DARK_AQUA", "◆, #3355ff,"})
  @DisplayName(
      "a mark is a shape in a colour, and draws as that shape in that colour in the rank font")
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
    assertEquals(RankMark.FONT, drawn.getStyle().getFont());
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

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"initiate", "hunter", "veteran", "master"})
  @DisplayName("every rank's mark has its own drawing in the rank font")
  void drawnInTheRankFont(final String rank) throws IOException {
    final String glyph =
        JsonParser.parseString(
                Files.readString(
                    Path.of(
                        "src/main/resources/data/nightborn/nightborn/guild_rank", rank + ".json")))
            .getAsJsonObject()
            .getAsJsonObject("mark")
            .get("glyph")
            .getAsString();
    final JsonObject drawing =
        JsonParser.parseString(
                Files.readString(Path.of("src/main/resources/assets/nightborn/font/rank.json")))
            .getAsJsonObject()
            .getAsJsonArray("providers")
            .asList()
            .stream()
            .map(JsonElement::getAsJsonObject)
            .filter(provider -> provider.has("chars"))
            .filter(
                provider ->
                    provider.getAsJsonArray("chars").asList().stream()
                        .anyMatch(chars -> chars.getAsString().equals(glyph)))
            .findFirst()
            .orElseThrow();
    assertEquals("nightborn:font/rank/" + rank + ".png", drawing.get("file").getAsString());
  }
}
