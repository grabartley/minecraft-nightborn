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

  @ParameterizedTest(name = "same home {0}, attacker a housemate {1}, {2} blocks: alerted {3}")
  @CsvSource({
    "true, false, 0, true",
    "true, false, 24, true",
    "true, false, 25, false",
    "false, false, 2, false",
    "true, true, 2, false"
  })
  @DisplayName("a hurt vampire's nearby housemates turn on its attacker, unless it is family")
  void alerted(
      final boolean sameHome,
      final boolean attackerIsHousemate,
      final double distance,
      final boolean alerted) {
    assertEquals(
        alerted, Housemates.alerted(sameHome, attackerIsHousemate, distance * distance, 24));
  }
}
