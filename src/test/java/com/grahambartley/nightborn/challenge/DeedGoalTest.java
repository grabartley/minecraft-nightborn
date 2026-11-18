package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeedGoalTest {

  static final Identifier FED = Identifier.of("nightborn", "fed");
  static final Identifier DOWNED = Identifier.of("nightborn", "downed");

  static DeedGoal goal(
      final int count,
      final DeedGoal.Measure measure,
      final DeedGoal.Within within,
      final boolean resets) {
    return new DeedGoal(
        new DeedMatch(FED, Map.of("animal", "true"), Map.of()),
        count,
        measure,
        measure == DeedGoal.Measure.DISTINCT ? Optional.of("prey") : Optional.empty(),
        within,
        resets ? Optional.of(DeedMatch.of(DOWNED)) : Optional.empty());
  }

  static Deed fed(final String prey) {
    return Deed.of(FED).with("animal", true).with("prey", prey);
  }

  @ParameterizedTest(name = "{0} feedings by {1} in one goal of 3 counts {2}")
  @CsvSource({
    "cow;pig;cow, TOTAL, 3",
    "cow;pig;cow, DISTINCT, 2",
    "cow;pig;sheep;goat, DISTINCT, 3",
    "cow, TOTAL, 1"
  })
  void countsTotalsOrDifferentKinds(
      final String preys, final DeedGoal.Measure measure, final int progress) {
    final DeedGoal goal = goal(3, measure, DeedGoal.Within.ANY_TIME, false);
    Tally tally = Tally.EMPTY;
    for (final String prey : preys.split(";")) {
      tally = goal.after(tally, fed(prey), OptionalLong.empty());
    }
    assertEquals(progress, goal.progress(tally));
  }

  @ParameterizedTest(name = "night {0} then night {1} leaves {2}")
  @CsvSource({"4, 4, 2", "4, 5, 1", "4, -1, 1"})
  void aOneNightGoalStartsAgainEachNightAndIgnoresTheDay(
      final long first, final long second, final int progress) {
    final DeedGoal goal = goal(5, DeedGoal.Measure.TOTAL, DeedGoal.Within.ONE_NIGHT, false);
    Tally tally = goal.after(Tally.EMPTY, fed("cow"), OptionalLong.of(first));
    tally =
        goal.after(tally, fed("cow"), second < 0 ? OptionalLong.empty() : OptionalLong.of(second));
    assertEquals(progress, goal.progress(tally));
  }

  @ParameterizedTest(name = "a reset after {0} feedings leaves nothing")
  @CsvSource({"1", "4"})
  void aResettingDeedClearsTheCount(final int feedings) {
    final DeedGoal goal = goal(10, DeedGoal.Measure.TOTAL, DeedGoal.Within.ANY_TIME, true);
    Tally tally = Tally.EMPTY;
    for (int i = 0; i < feedings; i++) {
      tally = goal.after(tally, fed("cow"), OptionalLong.empty());
    }
    assertEquals(0, goal.progress(goal.after(tally, Deed.of(DOWNED), OptionalLong.empty())));
  }

  @ParameterizedTest(name = "holding {0} then {1} of 60 is done: {2}")
  @CsvSource({"59, 12, false", "40, 60, true", "61, 3, true"})
  void aMostGoalKeepsTheBest(final int first, final int second, final boolean done) {
    final DeedGoal goal =
        new DeedGoal(
            DeedMatch.of(FED),
            60,
            DeedGoal.Measure.MOST,
            Optional.empty(),
            DeedGoal.Within.ANY_TIME,
            Optional.empty());
    final Tally tally =
        goal.after(
            goal.after(Tally.EMPTY, Deed.of(FED).times(first), OptionalLong.empty()),
            Deed.of(FED).times(second),
            OptionalLong.empty());
    assertEquals(done, goal.done(tally));
  }

  @ParameterizedTest(name = "a villager feeding never counts toward animals ({0})")
  @CsvSource({"TOTAL", "DISTINCT"})
  void otherDeedsLeaveTheTallyAlone(final DeedGoal.Measure measure) {
    final DeedGoal goal = goal(3, measure, DeedGoal.Within.ANY_TIME, false);
    assertEquals(
        Tally.EMPTY,
        goal.after(
            Tally.EMPTY,
            Deed.of(FED).with("animal", false).with("prey", "villager"),
            OptionalLong.empty()));
  }
}
