package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class HunterActionTest {

  private static final Path CLIPS =
      Path.of("src/client/resources/assets/nightborn/animations/entity/hunter.animation.json");
  private static final double TICKS_PER_SECOND = 20.0;

  private static JsonObject clips() throws IOException {
    return JsonParser.parseString(Files.readString(CLIPS))
        .getAsJsonObject()
        .getAsJsonObject("animations");
  }

  @ParameterizedTest(name = "sees {0}, {1} blocks, can throw {2}, crucifix {3}: {4}")
  @CsvSource({
    "false, 5.0, true, true, NONE",
    "true, 5.0, true, true, THROW_HOLY_WATER",
    "true, 4.0, true, false, THROW_HOLY_WATER",
    "true, 10.0, true, false, THROW_HOLY_WATER",
    "true, 10.5, true, true, NONE",
    "true, 3.9, true, true, RAISE_CRUCIFIX",
    "true, 3.9, true, false, NONE",
    "true, 5.0, false, true, RAISE_CRUCIFIX",
    "true, 6.0, false, true, RAISE_CRUCIFIX",
    "true, 6.1, false, true, NONE",
    "true, 5.0, false, false, NONE"
  })
  void supportsTheFightWithWhatTheKitHolds(
      final boolean sees,
      final double distance,
      final boolean canThrow,
      final boolean crucifix,
      final HunterAction action) {
    assertEquals(action, HunterAction.support(sees, distance, canThrow, crucifix, 6.0));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "-1, NONE",
    "0, NONE",
    "1, CROSSBOW_SHOT",
    "2, THROW_HOLY_WATER",
    "3, RAISE_CRUCIFIX",
    "4, NONE"
  })
  void readsAnActionFromItsSyncedIndex(final int index, final HunterAction action) {
    assertEquals(action, HunterAction.byIndex(index));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = HunterAction.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
  void theSharedAnimationsHoldEveryAction(final HunterAction action) throws IOException {
    assertTrue(clips().has(action.animation()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = HunterAction.class,
      names = {"CROSSBOW_SHOT", "THROW_HOLY_WATER"})
  void aTimedActionLastsAsLongAsItsClipAndReleasesInsideIt(final HunterAction action)
      throws IOException {
    final double seconds =
        clips().getAsJsonObject(action.animation()).get("animation_length").getAsDouble();
    assertTrue(action.timed());
    assertEquals(seconds * TICKS_PER_SECOND, action.lengthTicks(), 0.001);
    assertTrue(action.releaseTick() > 0 && action.releaseTick() < action.lengthTicks());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = HunterAction.class,
      names = {"NONE", "RAISE_CRUCIFIX"})
  void aHeldActionHasNoTimer(final HunterAction action) {
    assertEquals(false, action.timed());
  }
}
