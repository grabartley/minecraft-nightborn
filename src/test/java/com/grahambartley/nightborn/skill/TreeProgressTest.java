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

  @ParameterizedTest(name = "{1} experience on {0} banked gives {2} points and {3} banked")
  @CsvSource({
    "0, 50, 0, 50",
    "50, 50, 1, 0",
    "0, 203, 2, 0",
    "0, 300, 2, 97",
    "0, 0, 0, 0",
    "0, -4, 0, 0",
    "99.5, 0.5, 1, 0"
  })
  @DisplayName("experience banks toward points, and each point costs more than the last")
  void banksExperience(final float banked, final float gained, final int points, final float left) {
    final TreeProgress after =
        new TreeProgress(0, banked, List.of(), List.of()).addExperience(gained, 100, 3);
    assertEquals(points, after.earned());
    assertEquals(left, after.experience(), 0.0001F);
  }

  @ParameterizedTest(name = "bought {0} times: rank {0}")
  @CsvSource({"0", "1", "3"})
  @DisplayName("a skill's rank is how many times it was bought, and it is listed once")
  void ranks(final int times) {
    TreeProgress progress = TreeProgress.EMPTY.withEarned(20);
    for (int i = 0; i < times; i++) {
      progress = progress.buy(THICK, 1, null);
    }
    assertEquals(times, progress.rank(THICK));
    assertEquals(times > 0, progress.owns(THICK));
    assertEquals(times > 0 ? List.of(THICK) : List.of(), progress.ownedInOrder());
  }

  @Test
  @DisplayName("losing skills clears their slots on the power bar and keeps the rest")
  void keepingClearsTheBar() {
    final Identifier other = Identifier.of("nightborn", "core/other");
    final TreeProgress progress =
        TreeProgress.EMPTY
            .withEarned(5)
            .buy(THICK, 1, null)
            .buy(other, 1, null)
            .withBar(PowerLayout.inOrder(List.of(THICK, other)));
    final TreeProgress kept = progress.keeping(4, List.of(new BoughtSkill(THICK, 1)), List.of());
    assertEquals(PowerLayout.inOrder(List.of(THICK)), kept.bar().orElseThrow());
    assertEquals(4, kept.earned());
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
    assertEquals(List.of(NOBLE), twice.lines());
    assertEquals(List.of(THICK, Identifier.of("nightborn", "noble/other")), twice.ownedInOrder());
  }

  @Test
  @DisplayName(
      "a refund keeps earned points and banked experience but clears skills and bloodlines")
  void refund() {
    final TreeProgress refunded =
        new TreeProgress(6, 3.0F, List.of(new BoughtSkill(THICK, 2)), List.of(NOBLE)).refundAll();
    assertEquals(6, refunded.free());
    assertEquals(3.0F, refunded.experience());
    assertFalse(refunded.owns(THICK));
    assertTrue(refunded.lines().isEmpty());
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
        new TreeProgress(9, 4.25F, List.of(new BoughtSkill(THICK, 1)), List.of(NOBLE))
            .withBar(PowerLayout.inOrder(List.of(THICK)).place(3, NOBLE));
    final TreeProgress loaded =
        TreeProgress.CODEC
            .parse(
                JsonOps.INSTANCE,
                TreeProgress.CODEC.encodeStart(JsonOps.INSTANCE, progress).getOrThrow())
            .getOrThrow();
    assertEquals(progress, loaded);
  }

  @ParameterizedTest(name = "spent {0}, set to {1} -> {2}")
  @CsvSource({"3, 0, false", "3, 2, false", "3, 3, true", "3, 9, true", "0, 0, true"})
  @DisplayName("an operator cannot set points below what was already spent")
  void canSetEarned(final int spent, final int points, final boolean allowed) {
    final TreeProgress progress =
        new TreeProgress(
            10, 0.0F, spent == 0 ? List.of() : List.of(new BoughtSkill(THICK, spent)), List.of());
    assertEquals(allowed, progress.canSetEarned(points));
  }
}
