package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseStanceTest {

  @ParameterizedTest(name = "{0} blocks away: {1}")
  @CsvSource({
    "12.0, CLOSE",
    "9.01, CLOSE",
    "9.0, CIRCLE",
    "6.0, CIRCLE",
    "4.0, CIRCLE",
    "3.99, BACK",
    "0.0, BACK"
  })
  @DisplayName("a boss closes on a far target, backs off a near one and circles in between")
  void move(final double distance, final PhaseStance.Move move) {
    assertEquals(move, new PhaseStance(4.0F, 9.0F, 1.0, 25).move(distance));
  }

  @ParameterizedTest(name = "at tick {0}: {1}")
  @CsvSource({"0, 0.5", "24, 0.5", "25, -0.5", "49, -0.5", "50, 0.5"})
  @DisplayName("a circling boss changes direction every so many ticks")
  void side(final int age, final float side) {
    assertEquals(side, new PhaseStance(4.0F, 9.0F, 1.0, 25).side(age));
  }

  @ParameterizedTest(name = "an input of {0} at speed {1}: {2}")
  @CsvSource({
    "0.5, 1.0, 0.5",
    "0.5, 1.3, 0.65",
    "-0.6, 1.3, -0.78",
    "0.5, 3.0, 1.0",
    "-0.6, 3.0, -1.0"
  })
  @DisplayName("a faster stance sidesteps and backs off faster, up to a full stride")
  void paced(final float input, final double speed, final float paced) {
    assertEquals(paced, new PhaseStance(4.0F, 9.0F, speed, 25).paced(input), 1.0E-6F);
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | true",
        "{\"near\":2,\"far\":4,\"speed\":1.1,\"strafe_ticks\":30} | true",
        "{\"near\":5,\"far\":4} | false",
        "{\"speed\":0} | false",
        "{\"strafe_ticks\":0} | false",
        "{\"near\":-1} | false"
      })
  @DisplayName("a stance needs near no further than far, a speed and a strafe time")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, PhaseStance.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
