package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.HunterActionTimer.Step;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class HunterActionTimerTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = HunterAction.class,
      names = {"CROSSBOW_SHOT", "THROW_HOLY_WATER"})
  void releasesOnceAtTheReleaseTickAndEndsWithTheClip(final HunterAction action) {
    final HunterActionTimer timer = new HunterActionTimer();
    assertEquals(true, timer.begin(action));
    final List<Step> steps = new ArrayList<>();
    for (int tick = 0; tick < action.lengthTicks(); tick++) {
      steps.add(timer.tick());
    }
    assertEquals(action.releaseTick() - 1, steps.indexOf(Step.RELEASE));
    assertEquals(1, steps.stream().filter(Step.RELEASE::equals).count());
    assertEquals(Step.DONE, steps.getLast());
    assertEquals(false, timer.busy());
    assertEquals(Step.IDLE, timer.tick());
    assertEquals(HunterAction.NONE, timer.action());
  }

  @ParameterizedTest(name = "{0} then {1}: begins {2}")
  @CsvSource({
    "CROSSBOW_SHOT, THROW_HOLY_WATER, false",
    "THROW_HOLY_WATER, CROSSBOW_SHOT, false",
    "NONE, CROSSBOW_SHOT, true",
    "RAISE_CRUCIFIX, THROW_HOLY_WATER, true"
  })
  void aBusyTimerTakesNoSecondAction(
      final HunterAction first, final HunterAction second, final boolean begins) {
    final HunterActionTimer timer = new HunterActionTimer();
    timer.begin(first);
    assertEquals(begins, timer.begin(second));
    assertEquals(begins ? second : first, timer.action());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = HunterAction.class,
      names = {"NONE", "RAISE_CRUCIFIX"})
  void aHeldActionNeverStartsTheTimer(final HunterAction action) {
    final HunterActionTimer timer = new HunterActionTimer();
    assertEquals(false, timer.begin(action));
    assertEquals(false, timer.busy());
    assertEquals(Step.IDLE, timer.tick());
  }

  @ParameterizedTest(name = "after {0} ticks: can throw {1}")
  @CsvSource({"0, false", "16, false", "159, false", "160, true", "400, true"})
  void waitsBetweenThrows(final int ticks, final boolean ready) {
    final HunterActionTimer timer = new HunterActionTimer();
    assertEquals(true, timer.canThrow());
    assertEquals(true, timer.begin(HunterAction.THROW_HOLY_WATER));
    for (int tick = 0; tick < ticks; tick++) {
      timer.tick();
    }
    assertEquals(ready, timer.canThrow());
    assertEquals(ready, timer.begin(HunterAction.THROW_HOLY_WATER));
  }

  @ParameterizedTest(name = "{0}: throwing {1}")
  @CsvSource({
    "NONE, false",
    "CROSSBOW_SHOT, false",
    "THROW_HOLY_WATER, true",
    "RAISE_CRUCIFIX, false"
  })
  void onlyAHolyWaterThrowCountsAsThrowing(final HunterAction action, final boolean throwing) {
    final HunterActionTimer timer = new HunterActionTimer();
    timer.begin(action);
    assertEquals(throwing, timer.throwing());
  }
}
