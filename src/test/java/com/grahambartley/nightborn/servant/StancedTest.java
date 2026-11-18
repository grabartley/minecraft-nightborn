package com.grahambartley.nightborn.servant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StancedTest {

  @ParameterizedTest(name = "held {0}, shown {1} -> {2}")
  @CsvSource({
    "FOLLOW, FOLLOW, true",
    "STAY, FOLLOW, false",
    "GUARD, GUARD, true",
    ", STAY, false",
    ", GUARD, false"
  })
  @DisplayName("a servant holds only the stance it was given, and none when it has none")
  void holds(final Stance held, final Stance shown, final boolean expected) {
    final Stanced stanced =
        new Stanced() {
          @Override
          public Optional<Stance> stance() {
            return Optional.ofNullable(held);
          }

          @Override
          public BlockPos post() {
            return BlockPos.ORIGIN;
          }

          @Override
          public LivingEntity leaderHere() {
            return null;
          }

          @Override
          public boolean guardsAgainst(final LivingEntity intruder) {
            return false;
          }
        };
    assertEquals(expected, stanced.holds(shown));
  }
}
