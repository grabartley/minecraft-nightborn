package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TreeProgressTest {

  static final Identifier THICK = Identifier.of("nightborn", "core/thick_blood");
  static final Identifier NOBLE = Identifier.of("nightborn", "noble");

  @ParameterizedTest(
      name = "{1} blood at {2} per point from {0} banked gives {3} points and {4} banked")
  @CsvSource({
    "0, 10, 20, 0, 10",
    "10, 10, 20, 1, 0",
    "15, 50, 20, 3, 5",
    "0, 0, 20, 0, 0",
    "0, -4, 20, 0, 0",
    "0, 7.5, 1, 7, 0.5"
  })
  @DisplayName("blood banks toward points and whole points are paid out")
  void banksBlood(
      final float banked,
      final float worth,
      final int perPoint,
      final int points,
      final float left) {
    final TreeProgress after =
        new TreeProgress(0, banked, List.of(), List.of()).addBlood(worth, perPoint);
    assertEquals(points, after.earned());
    assertEquals(left, after.blood(), 0.0001F);
  }

  @ParameterizedTest(name = "earned {0}, paid {1} -> free {2}")
  @CsvSource({"5, 0, 5", "5, 3, 2", "5, 5, 0", "2, 5, 0"})
  @DisplayName("free points are earned minus spent, never below zero")
  void freePoints(final int earned, final int paid, final int free) {
    final TreeProgress progress =
        new TreeProgress(earned, 0.0F, List.of(new BoughtSkill(THICK, paid)), List.of());
    assertEquals(paid, progress.spent());
    assertEquals(free, progress.free());
  }

  @Test
  @DisplayName("buying records the skill, what was paid and a new bloodline once")
  void buying() {
    final TreeProgress once = TreeProgress.EMPTY.withEarned(5).buy(THICK, 2, NOBLE);
    final TreeProgress twice = once.buy(Identifier.of("nightborn", "noble/other"), 1, NOBLE);
    assertTrue(twice.owns(THICK));
    assertEquals(3, twice.spent());
    assertEquals(List.of(NOBLE), twice.bloodlines());
    assertEquals(List.of(THICK, Identifier.of("nightborn", "noble/other")), twice.ownedInOrder());
  }

  @Test
  @DisplayName("a refund keeps earned points and banked blood but clears skills and bloodlines")
  void refund() {
    final TreeProgress refunded =
        new TreeProgress(6, 3.0F, List.of(new BoughtSkill(THICK, 2)), List.of(NOBLE)).refundAll();
    assertEquals(6, refunded.free());
    assertEquals(3.0F, refunded.blood());
    assertFalse(refunded.owns(THICK));
    assertTrue(refunded.bloodlines().isEmpty());
  }

  @ParameterizedTest(name = "earned {0} stored as {1}")
  @CsvSource({"-3, 0", "0, 0", "40, 40", "200000, 100000"})
  @DisplayName("earned points are kept in bounds")
  void clamps(final int earned, final int stored) {
    assertEquals(stored, TreeProgress.EMPTY.withEarned(earned).earned());
  }

  @Test
  @DisplayName("progress survives a save and load unchanged")
  void roundTrips() {
    final TreeProgress progress =
        new TreeProgress(9, 4.25F, List.of(new BoughtSkill(THICK, 1)), List.of(NOBLE));
    final TreeProgress loaded =
        TreeProgress.CODEC
            .parse(
                JsonOps.INSTANCE,
                TreeProgress.CODEC.encodeStart(JsonOps.INSTANCE, progress).getOrThrow())
            .getOrThrow();
    assertEquals(progress, loaded);
  }
}
