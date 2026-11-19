package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerGiftsTest {

  @ParameterizedTest(name = "cost {0}, bat form {1}, cut {2}: {3}")
  @CsvSource({
    "3, true, -1.0, 2",
    "1, true, -1.0, 1",
    "2, true, -4.0, 1",
    "3, false, -1.0, 3",
    "0, true, -1.0, 0",
    "3, true, 0.0, 3"
  })
  @DisplayName("bat form costs less with the gift, never less than 1")
  void blood(final int cost, final boolean batForm, final double cut, final int paid) {
    assertEquals(paid, PowerGifts.blood(cost, batForm, cut));
  }

  @ParameterizedTest(name = "general {0}, tracking {1}, share {2}: {3}")
  @CsvSource({"1.0, true, 0.8, 0.8", "1.0, false, 0.8, 1.0", "0.92, true, 0.8, 0.736"})
  @DisplayName("tracking's own cooldown share applies only to tracking")
  void cooldown(
      final double general, final boolean tracking, final double share, final double factor) {
    assertEquals(factor, PowerGifts.cooldown(general, tracking, share), 1.0E-9);
  }

  @ParameterizedTest(name = "cost {0}, bat {1}, bat cut {2}, line cut {3}: {4}")
  @CsvSource({
    "6, false, 0.0, 1, 5",
    "1, false, 0.0, 1, 1",
    "3, true, -1.0, 1, 1",
    "0, false, 0.0, 1, 0",
    "4, false, 0.0, 0, 4"
  })
  @DisplayName("a bloodline gift cuts a power's blood, never below one")
  void bloodlineBlood(
      final int cost, final boolean bat, final double batCut, final int lineCut, final int paid) {
    assertEquals(paid, PowerGifts.blood(cost, bat, batCut, lineCut));
  }

  @ParameterizedTest(name = "general {0}, line cut {1}: {2}")
  @CsvSource({"1.0, 0.2, 0.8", "0.9, 0.2, 0.72", "0.2, 0.2, 0.2", "1.0, 0.0, 1.0"})
  @DisplayName("a bloodline gift shortens cooldowns on top of the rest, never past 80%")
  void bloodlineCooldown(final double general, final double cut, final double factor) {
    assertEquals(factor, PowerGifts.cooldown(general, false, 1.0, cut), 1.0E-9);
  }

  @ParameterizedTest(name = "cuts {0} and {1}")
  @CsvSource({"0.2, 0.2, 0.4", "0.5, 0.5, 0.8"})
  @DisplayName("gifts add up, never past the cap")
  void sums(final double one, final double two, final double total) {
    final List<PowerGift> gifts =
        List.of(
            new PowerGift(Identifier.of("nightborn:beast"), 1, one, false),
            new PowerGift(Identifier.of("nightborn:beast"), 1, two, false));
    assertEquals(total, PowerGifts.cooldownCut(gifts), 1.0E-9);
    assertEquals(2, PowerGifts.bloodCut(gifts));
  }

  @ParameterizedTest(name = "refunds {0} and {1}: {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true"})
  @DisplayName("a dodge gives a dash back when any gift for its path says so")
  void refundsOnDodge(final boolean one, final boolean two, final boolean refunds) {
    assertEquals(
        refunds,
        PowerGifts.refundsOnDodge(
            List.of(
                new PowerGift(Identifier.of("nightborn:slayer"), 0, 0.2, one),
                new PowerGift(Identifier.of("nightborn:slayer"), 0, 0.0, two))));
  }

  @ParameterizedTest(name = "cut {0}: factor {1}")
  @CsvSource({"0.2, 0.8", "0.0, 1.0", "0.9, 0.2"})
  @DisplayName("a path's cut shortens a cooldown that no general factor touches, down to the floor")
  void branchCooldown(final double cut, final double factor) {
    assertEquals(factor, PowerGifts.branchCooldown(cut), 1.0E-9);
  }
}
