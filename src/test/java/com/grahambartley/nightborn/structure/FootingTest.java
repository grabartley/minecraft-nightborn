package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ColumnPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class FootingTest {

  private static final Footing DRY = new Footing(true, 4, 8, Optional.empty(), Optional.empty());

  static Stream<Arguments> grounds() {
    final Footing.Column field = new Footing.Column(70, 70);
    return Stream.of(
        Arguments.of(
            "level dry ground", DRY, List.of(field, field, new Footing.Column(72, 72)), true),
        Arguments.of(
            "one corner in a river", DRY, List.of(field, new Footing.Column(63, 60)), false),
        Arguments.of(
            "a river is fine when water is allowed",
            new Footing(false, 10, 8, Optional.empty(), Optional.empty()),
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
    final List<ColumnPos> samples =
        new Footing(true, 4, spacing, Optional.empty(), Optional.empty())
            .samples(new BlockBox(0, 0, 0, width - 1, 10, width - 1), new ColumnPos(-3, -4));
    assertEquals(perSide * perSide + 1, samples.size());
    assertEquals(new ColumnPos(-3, -4), samples.getFirst());
    assertTrue(samples.stream().anyMatch(p -> p.x() == width - 1 && p.z() == width - 1));
    assertTrue(samples.stream().anyMatch(p -> p.x() == 0 && p.z() == 0));
  }

  @Test
  void dataReadsWithDryAndSpacingDefaults() {
    final Footing footing =
        Footing.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"max_rise\": 4}"))
            .getOrThrow();
    assertEquals(new Footing(true, 4, 8, Optional.empty(), Optional.empty()), footing);
  }

  @Test
  void dataNamesTheEntranceToLevelWithTheGround() {
    final Footing footing =
        Footing.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"max_rise\": 6, \"entrance\": \"nightborn:castle_entrance\"}"))
            .getOrThrow();
    assertEquals(
        new Footing(
            true,
            6,
            8,
            Optional.of(Identifier.of("nightborn", "castle_entrance")),
            Optional.empty()),
        footing);
  }

  @ParameterizedTest(name = "rise {0}, lift limit {1}: a move of {2} is allowed: {3}")
  @CsvSource({
    "4, , 4, true",
    "4, , -40, true",
    "30, 10, 10, true",
    "30, 10, -11, false",
    "30, 0, 0, true",
    "30, 0, 1, false"
  })
  void aStructureMovesOnlyAsFarAsItsDataAllows(
      final int rise, final Integer limit, final int lift, final boolean allowed) {
    assertEquals(
        allowed,
        new Footing(true, rise, 8, Optional.empty(), Optional.ofNullable(limit)).allowsLift(lift));
  }
}
