package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BodyZonesTest {

  @ParameterizedTest(name = "height {0}, side {1}: {2}")
  @CsvSource({
    "0.95, 0.0, HEAD",
    "0.75, 0.9, HEAD",
    "0.74, 0.9, RIGHT_ARM",
    "0.5, -0.6, LEFT_ARM",
    "0.5, 0.5, ",
    "0.5, -0.2, ",
    "0.374, 0.1, RIGHT_LEG",
    "0.0, -0.1, LEFT_LEG",
    "0.2, 0.0, RIGHT_LEG"
  })
  @DisplayName("the default zones split the body into head, arms, trunk and legs")
  void zones(final double height, final double side, final BodyPart part) {
    assertEquals(Optional.ofNullable(part), BodyZones.DEFAULT.at(height, side));
  }

  @ParameterizedTest(name = "roll {0}: {1}")
  @CsvSource({
    "0.0, HEAD",
    "0.05, HEAD",
    "0.06, RIGHT_ARM",
    "0.3, RIGHT_ARM",
    "0.32, LEFT_ARM",
    "0.56, LEFT_ARM",
    "0.58, RIGHT_LEG",
    "0.99, LEFT_LEG",
    "1.0, LEFT_LEG"
  })
  @DisplayName("a mob's blow picks a part by weight, limbs far more often than the head")
  void weighted(final double roll, final BodyPart part) {
    assertEquals(part, BodyZones.DEFAULT.weighted(roll));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"head\":0.5,\"hip\":0.5,\"trunk\":0.5,\"mob_weights\":{\"right_arm\":1}}|leave no room",
        "{\"head\":0.2,\"hip\":0.3,\"trunk\":0.5,\"mob_weights\":{\"head\":3}}|at least one limb",
        "{\"head\":0.2,\"hip\":0.3,\"trunk\":1.0,\"mob_weights\":{\"right_arm\":3}}|no room for the arms",
        "{\"head\":1.5,\"hip\":0.3,\"trunk\":0.5,\"mob_weights\":{\"head\":3}}|1.5",
        "{\"head\":0.2,\"hip\":0.3,\"trunk\":0.5,\"mob_weights\":{\"tail\":3}}|tail"
      })
  @DisplayName("a bad zone file is refused with a message that names what is wrong")
  void refusesBadData(final String json, final String named) {
    final String error =
        BodyZones.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .error()
            .orElseThrow()
            .message();
    assertTrue(error.contains(named), error);
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"HEAD, 0", "RIGHT_ARM, 2", "LEFT_LEG, 0"})
  @DisplayName("a part left out of the weights has no weight")
  void missingWeight(final BodyPart part, final int weight) {
    assertEquals(
        weight, new BodyZones(0.25, 0.375, 0.5, Map.of(BodyPart.RIGHT_ARM, 2)).weight(part));
  }
}
