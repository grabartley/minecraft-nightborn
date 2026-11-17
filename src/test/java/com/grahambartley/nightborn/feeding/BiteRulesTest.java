package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.feeding.BiteRules.Biter;
import com.grahambartley.nightborn.feeding.BiteRules.Decision;
import com.grahambartley.nightborn.feeding.BiteRules.Target;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class BiteRulesTest {

  private static final Biter READY = new Biter(true, true, true, false);
  private static final Target COW = new Target(true, false, false, true, false, false);
  private static final Target HUMAN = new Target(true, false, true, true, false, false);

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of("a ready vampire bites a cow", READY, COW, false, Decision.BITE),
        Arguments.of(
            "a human cannot bite",
            new Biter(false, true, true, false),
            COW,
            true,
            Decision.NOT_A_VAMPIRE),
        Arguments.of(
            "standing up is not a bite",
            new Biter(true, false, true, false),
            COW,
            true,
            Decision.NOT_SNEAKING_EMPTY_HANDED),
        Arguments.of(
            "a held item is not a bite",
            new Biter(true, true, false, false),
            COW,
            true,
            Decision.NOT_SNEAKING_EMPTY_HANDED),
        Arguments.of(
            "a downed vampire cannot feed",
            new Biter(true, true, true, true),
            COW,
            true,
            Decision.HELPLESS),
        Arguments.of(
            "no blood in a zombie",
            READY,
            new Target(false, false, false, true, false, false),
            true,
            Decision.NO_WARM_BLOOD),
        Arguments.of(
            "a vampire's blood is already dead",
            READY,
            new Target(true, true, true, true, false, false),
            true,
            Decision.NO_WARM_BLOOD),
        Arguments.of(
            "a dead victim gives nothing",
            READY,
            new Target(true, false, false, false, false, false),
            true,
            Decision.NO_WARM_BLOOD),
        Arguments.of("a human player with PvP on", READY, HUMAN, true, Decision.BITE),
        Arguments.of("a human player with PvP off", READY, HUMAN, false, Decision.PVP_OFF),
        Arguments.of(
            "a human holding a crucifix",
            READY,
            new Target(true, false, true, true, true, false),
            true,
            Decision.WARDED),
        Arguments.of(
            "a crucifix wards even with PvP off",
            READY,
            new Target(true, false, true, true, true, false),
            false,
            Decision.WARDED),
        Arguments.of(
            "a downed vampire is helpless before the crucifix matters",
            new Biter(true, true, true, true),
            new Target(true, false, true, true, true, false),
            true,
            Decision.HELPLESS),
        Arguments.of(
            "standing over a badly hurt cow is a finisher bite",
            new Biter(true, false, true, false),
            new Target(true, false, false, true, false, true),
            false,
            Decision.FINISH),
        Arguments.of(
            "sneaking over a badly hurt cow is still a plain bite",
            READY,
            new Target(true, false, false, true, false, true),
            false,
            Decision.BITE),
        Arguments.of(
            "a held item is never a finisher",
            new Biter(true, false, false, false),
            new Target(true, false, false, true, false, true),
            true,
            Decision.NOT_SNEAKING_EMPTY_HANDED),
        Arguments.of(
            "a finisher on a player needs PvP",
            new Biter(true, false, true, false),
            new Target(true, false, true, true, false, true),
            false,
            Decision.PVP_OFF),
        Arguments.of(
            "a finisher on a player with PvP on",
            new Biter(true, false, true, false),
            new Target(true, false, true, true, false, true),
            true,
            Decision.FINISH),
        Arguments.of(
            "no finisher on a creature with no blood",
            new Biter(true, false, true, false),
            new Target(false, false, false, true, false, true),
            true,
            Decision.NO_WARM_BLOOD),
        Arguments.of(
            "a downed vampire cannot finish",
            new Biter(true, false, true, true),
            new Target(true, false, false, true, false, true),
            true,
            Decision.HELPLESS));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  @DisplayName("the server decides who may bite whom")
  void decides(
      final String name,
      final Biter biter,
      final Target target,
      final boolean pvp,
      final Decision expected) {
    assertEquals(expected, BiteRules.decide(biter, target, pvp));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Decision.class)
  @DisplayName(
      "only a bite or a finisher drinks, and a refused bite still stops other interactions")
  void claims(final Decision decision) {
    assertEquals(decision == Decision.BITE, decision.bites());
    assertEquals(decision == Decision.FINISH, decision.finishes());
    assertEquals(
        decision == Decision.BITE
            || decision == Decision.FINISH
            || decision == Decision.PVP_OFF
            || decision == Decision.HELPLESS
            || decision == Decision.WARDED,
        decision.claimsTheInteraction());
  }

  @ParameterizedTest(name = "warm blood {0}, vampire {1}: drinkable {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  @DisplayName("only warm blood that is not a vampire's can be drunk")
  void drinkable(final boolean warmBlood, final boolean vampire, final boolean drinkable) {
    assertEquals(drinkable, BiteRules.drinkable(warmBlood, vampire));
  }

  @ParameterizedTest(name = "human {0}, turning {1}, allowed {2}, first {3}, roll {4} of {5}: {6}")
  @CsvSource({
    "true, true, true, true, 999, 1000, true",
    "true, true, true, true, 500, 500, false",
    "true, true, true, true, 499, 500, true",
    "true, true, true, true, 0, 1, true",
    "true, true, true, true, 1, 1, false",
    "false, true, true, true, 0, 1000, false",
    "true, false, true, true, 0, 1000, false",
    "true, true, false, true, 0, 1000, false",
    "true, true, true, false, 0, 1000, false"
  })
  @DisplayName(
      "a bite starts sickness only in a human, as a turning bite, when allowed, once, and by chance")
  void startsSickness(
      final boolean human,
      final boolean turning,
      final boolean allowed,
      final boolean first,
      final int roll,
      final int chance,
      final boolean starts) {
    assertEquals(starts, BiteRules.startsSickness(human, turning, allowed, first, roll, chance));
  }

  @ParameterizedTest(name = "{0}% is {1} per thousand")
  @CsvSource({"0, 0", "1, 10", "50, 500", "100, 1000"})
  @DisplayName("a percent chance reads as the same chance per thousand")
  void perThousand(final int percent, final int perThousand) {
    assertEquals(perThousand, BiteRules.perThousand(percent));
  }

  @ParameterizedTest(name = "{1}% of a {0} bar costs {2}")
  @CsvSource({"20, 50, 10", "21, 50, 11", "60, 50, 30", "20, 0, 0", "20, 100, 20", "0, 50, 0"})
  @DisplayName("a turning bite costs its share of a full bar, rounded up")
  void turningCost(final int fullBar, final int percent, final int cost) {
    assertEquals(cost, BiteRules.turningCost(fullBar, percent));
  }

  @ParameterizedTest(name = "{0} of {1} below {2}%: {3}")
  @CsvSource({
    "4.9, 20, 25, true",
    "5.0, 20, 25, false",
    "20, 20, 25, false",
    "1, 20, 25, true",
    "0, 20, 25, false",
    "99, 100, 100, true",
    "5, 0, 25, false"
  })
  @DisplayName("a finisher bite opens only below the threshold share of health")
  void finishable(
      final float health, final float max, final int threshold, final boolean finishable) {
    assertEquals(finishable, BiteRules.finishable(health, max, threshold));
  }
}
