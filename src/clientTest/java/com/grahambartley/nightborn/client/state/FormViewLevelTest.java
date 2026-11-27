package com.grahambartley.nightborn.client.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.formview.FormView;
import com.grahambartley.nightborn.formview.FormViewEcho;
import com.grahambartley.nightborn.formview.FormViewTint;
import com.grahambartley.nightborn.formview.FormViewTremble;
import com.grahambartley.nightborn.formview.FormViewWash;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class FormViewLevelTest {

  private static final FormViewEcho ECHO =
      new FormViewEcho(5.0F, 0.3F, 24.0F, 2.0F, 16.0F, 40.0F, 1.5F, 1.0F, 0.1F, 0.6F, 30, 0.6F);

  private static final FormView VIEW =
      new FormView(
          Identifier.of("nightborn", "test_form"),
          0,
          10,
          10,
          0.8F,
          Optional.of(ECHO),
          Optional.of(new FormViewTint(0x9E1418, 0.2F, 0.6F)),
          Optional.of(new FormViewTremble(0.006F, 0.45F, 9.0F)),
          Optional.of(new FormViewWash(0xC9CED3, 0.4F, 0.3F, 2.0F, 0.004F)));

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({"FULL, full", "REDUCED, reduced", "OFF, off"})
  @DisplayName("each level has a lower-case id that finds it again")
  void idsRoundTrip(final FormViewLevel level, final String id) {
    assertEquals(id, level.id());
    assertEquals(Optional.of(level), FormViewLevel.byId(id));
  }

  @ParameterizedTest(name = "\"{0}\"")
  @ValueSource(strings = {"", "FULL", "none", "reduce", " off"})
  @DisplayName("an unknown id finds no level")
  void unknownIds(final String id) {
    assertEquals(Optional.empty(), FormViewLevel.byId(id));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(FormViewLevel.class)
  @DisplayName("each level has its English name")
  void hasEnglishText(final FormViewLevel level) throws IOException {
    final JsonObject english =
        JsonParser.parseString(
                Files.readString(
                    Path.of("src/main/resources/assets/nightborn/lang/en_us.json"),
                    StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertTrue(english.has(level.translationKey()), level.translationKey());
  }

  @Test
  @DisplayName("full draws the view exactly as the data says")
  void fullKeepsView() {
    assertEquals(Optional.of(VIEW), FormViewLevel.FULL.look(VIEW));
  }

  @Test
  @DisplayName("off draws no view at all")
  void offDrawsNothing() {
    assertEquals(Optional.empty(), FormViewLevel.OFF.look(VIEW));
  }

  @Test
  @DisplayName("reduced removes the tremble")
  void reducedRemovesTremble() {
    assertEquals(Optional.empty(), FormViewLevel.REDUCED.look(VIEW).orElseThrow().tremble());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"darkness, 0.4", "tint centre, 0.1", "tint edge, 0.3", "wash amount, 0.2"})
  @DisplayName("reduced softens the dark, the tint and the wash by half")
  void reducedSoftens(final String part, final float expected) {
    final FormView reduced = FormViewLevel.REDUCED.look(VIEW).orElseThrow();
    final float actual =
        switch (part) {
          case "darkness" -> reduced.darkness();
          case "tint centre" -> reduced.tint().orElseThrow().centre();
          case "tint edge" -> reduced.tint().orElseThrow().edge();
          case "wash amount" -> reduced.wash().orElseThrow().amount();
          default -> throw new IllegalArgumentException(part);
        };
    assertEquals(expected, actual, 1.0E-6F);
  }

  @Test
  @DisplayName("reduced keeps the echo, the form and the fade times")
  void reducedKeepsTheRest() {
    final FormView reduced = FormViewLevel.REDUCED.look(VIEW).orElseThrow();
    assertEquals(VIEW.echo(), reduced.echo());
    assertEquals(VIEW.form(), reduced.form());
    assertEquals(VIEW.fadeInTicks(), reduced.fadeInTicks());
    assertEquals(VIEW.fadeOutTicks(), reduced.fadeOutTicks());
    assertEquals(0x9E1418, reduced.tint().orElseThrow().color());
  }
}
