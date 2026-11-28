package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MindTest {

  private static Mind mind(final float blur, final float foresight, final float thrift) {
    return new Mind(
        30,
        new Sharpness(blur, EnumSet.allOf(Sharpness.Reads.class), foresight),
        new Disposition(Map.of(Intent.FIGHT, 1.5F), Map.of(), thrift));
  }

  @ParameterizedTest(name = "base thrift {0}, foresight {1}, disposition thrift {2}: {3}")
  @CsvSource({
    "0.5, 1, 1, 0.5",
    "0.5, 0.25, 1, 0.125",
    "0.5, 0.25, 0.5, 0.0625",
    "0.5, 1.6, 1.6, 1",
    "0, 2, 4, 0"
  })
  @DisplayName(
      "a far-sighted mob saves its blood and cooldowns more, a short-sighted one spends them,"
          + " within the budget's range")
  void budget(final float base, final float foresight, final float thrift, final float kept) {
    final IntentBudget budget = new IntentBudget(4, 60, 0.2F, base, 0.1F, 2);
    final IntentBudget minded = mind(0.0F, foresight, thrift).budget(budget);
    assertEquals(kept, minded.thrift(), 1.0E-6);
    assertEquals(budget.withThrift(minded.thrift()), minded);
  }

  @ParameterizedTest(name = "blur {0}, {1} at {2}, roll {3}: {4}")
  @CsvSource({
    "0, FIGHT, TARGET, 0, 1.5",
    "0, RETREAT, TARGET, 0.9, 1",
    "0.5, FIGHT, SELF, 0, 0.75",
    "0.5, RETREAT, SELF, 1, 1.5"
  })
  @DisplayName("a score is leaned by the mob's disposition, then blurred by its sharpness")
  void judged(
      final float blur,
      final Intent intent,
      final Aim aim,
      final double roll,
      final double judged) {
    assertEquals(judged, mind(blur, 1.0F, 1.0F).judged(1.0, intent, aim, roll), 1.0E-6);
  }

  @ParameterizedTest(name = "reads group {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a mob helps allies only when it reads the group")
  void readsGroup(final boolean group) {
    final Mind mind =
        new Mind(
            30,
            new Sharpness(
                0.0F,
                group ? EnumSet.of(Sharpness.Reads.GROUP) : EnumSet.noneOf(Sharpness.Reads.class),
                1.0F),
            Disposition.EVEN);
    assertEquals(group, mind.readsGroup());
    assertEquals(
        group ? Set.of(Fact.OWN_HEALTH, Fact.ALLY_HEALTH) : Set.of(Fact.OWN_HEALTH),
        mind.reads(EnumSet.of(Fact.OWN_HEALTH, Fact.ALLY_HEALTH)));
  }

  @ParameterizedTest(name = "think ticks {0}")
  @CsvSource({"12", "40"})
  @DisplayName("a plain mind thinks at its pace, keenly and evenly, as mobs did before")
  void plain(final int thinkTicks) {
    assertEquals(new Mind(thinkTicks, Sharpness.KEEN, Disposition.EVEN), Mind.plain(thinkTicks));
  }
}
