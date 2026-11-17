package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.ammo.GarlicFumesEffect;
import com.grahambartley.nightborn.blood.Starvation;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.skill.SkillPower;
import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.WeakenedSources;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BatFormRulesTest {

  @ParameterizedTest(
      name = "sun {0}, downed {1}, warded {2}, blood {3}, owns form {4} -> forced out {5}")
  @CsvSource({
    "false, false, false, 10, true, false",
    "true, false, false, 10, true, true",
    "false, true, false, 10, true, true",
    "false, false, true, 10, true, true",
    "false, false, false, 0, true, true",
    "false, false, false, 1, true, false",
    "false, false, false, 10, false, true"
  })
  @DisplayName(
      "sun, being downed, garlic or holy water, an empty blood bar or losing the skill force a bat"
          + " back")
  void forcedOut(
      final boolean sun,
      final boolean downed,
      final boolean warded,
      final int blood,
      final boolean ownsForm,
      final boolean expected) {
    assertEquals(expected, BatFormRules.forcedOut(sun, downed, warded, blood, ownsForm));
  }

  static final Path BAT_FORM =
      Path.of("src/main/resources/data/nightborn/nightborn/skill/beast/bat_form.json");

  static List<String> shippedForcedBy() {
    try {
      return SkillPower.CODEC
          .parse(
              JsonOps.INSTANCE,
              JsonParser.parseString(Files.readString(BAT_FORM)).getAsJsonObject().get("power"))
          .getOrThrow()
          .batForm()
          .orElseThrow()
          .forcedBy();
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
  }

  static Stream<Arguments> sources() {
    return Stream.of(
        Arguments.of(Wards.STRINGS, 1, true),
        Arguments.of(Wards.TOUCH, 2, true),
        Arguments.of(GarlicFumesEffect.WEAKENED_SOURCE, 1, true),
        Arguments.of(HolyWater.WEAKENED_SOURCE, 1, true),
        Arguments.of(Wards.CRUCIFIX, 3, false),
        Arguments.of(Wards.SHAPE, 3, false),
        Arguments.of(Starvation.SOURCE, 2, false),
        Arguments.of(Wards.STRINGS, 0, false));
  }

  @ParameterizedTest(name = "{0} at {1} -> {2}")
  @MethodSource("sources")
  @DisplayName(
      "the shipped bat form is forced out by garlic, from strings, a touch or a bomb, and holy"
          + " water only")
  void warded(final String source, final int strength, final boolean expected) {
    assertEquals(
        expected,
        BatFormRules.warded(WeakenedSources.NONE.with(source, strength, 20), shippedForcedBy()));
  }

  @ParameterizedTest(name = "may fly {0}, bat {1} -> {2}")
  @CsvSource({
    "false, false, false",
    "true, false, true",
    "false, true, false",
    "true, true, false"
  })
  @DisplayName("a bat never flies as a creative player does")
  void creativeFlight(final boolean allowFlying, final boolean bat, final boolean expected) {
    assertEquals(expected, BatFormRules.creativeFlight(allowFlying, bat));
  }

  @ParameterizedTest(name = "on ground {0}, floor below {1} -> {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a bat is perched only when it says it landed and there is a floor under it")
  void perched(final boolean onGround, final boolean floorBelow, final boolean expected) {
    assertEquals(expected, BatFormRules.perched(onGround, floorBelow));
  }

  @ParameterizedTest(name = "{0} damage against {1} -> {2}")
  @CsvSource({"5.9, 6, false", "6.0, 6, true", "12.0, 6, true", "1.0, 1, true", "0.5, 1, false"})
  @DisplayName("a blow at or above the heavy damage setting forces a bat back")
  void heavyHit(final float damage, final int heavy, final boolean expected) {
    assertEquals(expected, BatFormRules.heavyHit(damage, heavy));
  }
}
