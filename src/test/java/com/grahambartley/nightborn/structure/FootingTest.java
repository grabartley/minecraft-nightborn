package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class FootingTest {

  private static final Footing DRY = new Footing(true, 4, 8);

  static Stream<Arguments> grounds() {
    final Footing.Column field = new Footing.Column(70, 70);
    return Stream.of(
        Arguments.of(
            "level dry ground", DRY, List.of(field, field, new Footing.Column(72, 72)), true),
        Arguments.of(
            "one corner in a river", DRY, List.of(field, new Footing.Column(63, 60)), false),
        Arguments.of(
            "a river is fine when water is allowed",
            new Footing(false, 10, 8),
            List.of(field, new Footing.Column(63, 60)),
            true),
        Arguments.of(
            "a slope just within the rise", DRY, List.of(field, new Footing.Column(74, 74)), true),
        Arguments.of(
            "a slope too steep for the bank",
            DRY,
            List.of(field, new Footing.Column(75, 75)),
            false),
        Arguments.of("nowhere sampled", DRY, List.of(), false));
  }

  @ParameterizedTest(name = "{0}: accepted is {3}")
  @MethodSource("grounds")
  void acceptsOnlyDryGroundWithinTheRise(
      final String name,
      final Footing footing,
      final List<Footing.Column> columns,
      final boolean accepted) {
    assertEquals(accepted, footing.accepts(columns));
  }

  @ParameterizedTest(name = "a box {0} wide sampled every {1} has {2} points a side")
  @CsvSource({"38, 8, 6", "16, 8, 3", "1, 8, 1", "17, 16, 2"})
  void samplesReachEveryEdgeOfTheBox(final int width, final int spacing, final int perSide) {
    final List<int[]> samples =
        new Footing(true, 4, spacing).samples(new BlockBox(0, 0, 0, width - 1, 10, width - 1));
    assertEquals(perSide * perSide, samples.size());
    assertTrue(samples.stream().anyMatch(p -> p[0] == width - 1 && p[1] == width - 1));
    assertTrue(samples.stream().anyMatch(p -> p[0] == 0 && p[1] == 0));
  }

  @Test
  void dataReadsWithDryAndSpacingDefaults() {
    final Footing footing =
        Footing.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"max_rise\": 4}"))
            .getOrThrow();
    assertEquals(new Footing(true, 4, 8), footing);
  }
}
