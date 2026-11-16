package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class HousematesTest {

  static Stream<Arguments> homes() {
    final Optional<BlockPos> home = Optional.of(new BlockPos(10, 64, -3));
    return Stream.of(
        Arguments.of(home, Optional.of(new BlockPos(10, 64, -3)), true),
        Arguments.of(home, Optional.of(new BlockPos(11, 64, -3)), false),
        Arguments.of(home, Optional.empty(), false),
        Arguments.of(Optional.empty(), Optional.empty(), false));
  }

  @ParameterizedTest(name = "{0} and {1}: housemates {2}")
  @MethodSource("homes")
  @DisplayName("vampires are housemates only when they share a home, never when homeless")
  void share(
      final Optional<BlockPos> one, final Optional<BlockPos> other, final boolean housemates) {
    assertEquals(housemates, Housemates.share(one, other));
  }

  @ParameterizedTest(name = "same home {0}, attacker a vampire {1}, {2} blocks: alerted {3}")
  @CsvSource({
    "true, false, 0, true",
    "true, false, 24, true",
    "true, false, 25, false",
    "false, false, 2, false",
    "true, true, 2, false"
  })
  @DisplayName("a hurt vampire's nearby housemates turn on its attacker, unless it is a vampire")
  void alerted(
      final boolean sameHome,
      final boolean attackerIsVampire,
      final double distance,
      final boolean alerted) {
    assertEquals(alerted, Housemates.alerted(sameHome, attackerIsVampire, distance * distance, 24));
  }

  @ParameterizedTest(
      name = "same home {0}, foe a vampire {1}, asleep {2}, helpless {3}, {4} blocks: {5}")
  @CsvSource({
    "true, false, false, false, 10, true",
    "true, false, false, false, 32, true",
    "true, false, false, false, 33, false",
    "false, false, false, false, 4, false",
    "true, true, false, false, 4, false",
    "true, false, true, false, 4, false",
    "true, false, false, true, 4, false"
  })
  @DisplayName(
      "a head's call rallies only awake housemates who can still fight, never on a vampire")
  void rallied(
      final boolean sameHome,
      final boolean foeIsVampire,
      final boolean asleep,
      final boolean helpless,
      final int blocks,
      final boolean rallied) {
    assertEquals(
        rallied,
        Housemates.rallied(sameHome, foeIsVampire, asleep, helpless, (double) blocks * blocks, 32));
  }
}
