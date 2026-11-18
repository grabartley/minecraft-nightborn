package com.grahambartley.nightborn.faction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class OathTest {

  @ParameterizedTest(name = "proven {0}, sworn {1}, vampire {2}, old points {3} -> hunter {4}")
  @CsvSource({
    "false, false, false, false, false",
    "true, false, false, false, false",
    "true, true, false, false, true",
    "false, false, false, true, true",
    "true, true, true, false, false",
    "false, false, true, true, false"
  })
  @DisplayName("a hunter is a human who took the oath or kept hunter points from an older world")
  void hunter(
      final boolean proven,
      final boolean sworn,
      final boolean vampire,
      final boolean oldPoints,
      final boolean hunter) {
    assertEquals(hunter, new Oath(proven, sworn).hunter(vampire, oldPoints));
  }

  @ParameterizedTest(name = "proven {0}, sworn {1}, vampire {2}, old points {3} -> may swear {4}")
  @CsvSource({
    "false, false, false, false, false",
    "true, false, false, false, true",
    "true, true, false, false, false",
    "true, false, true, false, false",
    "true, false, false, true, false"
  })
  @DisplayName("only a proven human who is not yet a hunter may be sworn in")
  void maySwear(
      final boolean proven,
      final boolean sworn,
      final boolean vampire,
      final boolean oldPoints,
      final boolean may) {
    assertEquals(may, new Oath(proven, sworn).maySwear(vampire, oldPoints));
  }

  static Stream<Arguments> steps() {
    return Stream.of(
        Arguments.of(Oath.NONE, new Oath(true, false), new Oath(true, true)),
        Arguments.of(new Oath(true, false), new Oath(true, false), new Oath(true, true)),
        Arguments.of(new Oath(false, true), new Oath(true, true), new Oath(true, true)));
  }

  @ParameterizedTest(name = "{0}: proved {1}, sworn {2}")
  @MethodSource("steps")
  @DisplayName("proving and swearing only ever move forward")
  void steps(final Oath from, final Oath proved, final Oath sworn) {
    assertEquals(proved, from.prove());
    assertEquals(sworn, from.swear());
  }

  static Stream<Oath> oaths() {
    return Stream.of(Oath.NONE, new Oath(true, false), new Oath(true, true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("oaths")
  @DisplayName("an oath saves and loads unchanged")
  void roundTrips(final Oath oath) {
    final var json = Oath.CODEC.encodeStart(JsonOps.INSTANCE, oath).getOrThrow();
    assertEquals(oath, Oath.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
  }
}
