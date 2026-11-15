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
  private static final Target COW = new Target(true, false, false, true, false);
  private static final Target HUMAN = new Target(true, false, true, true, false);

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
            new Target(false, false, false, true, false),
            true,
            Decision.NO_WARM_BLOOD),
        Arguments.of(
            "a vampire's blood is already dead",
            READY,
            new Target(true, true, true, true, false),
            true,
            Decision.NO_WARM_BLOOD),
        Arguments.of(
            "a dead victim gives nothing",
            READY,
            new Target(true, false, false, false, false),
            true,
            Decision.NO_WARM_BLOOD),
        Arguments.of("a human player with PvP on", READY, HUMAN, true, Decision.BITE),
        Arguments.of("a human player with PvP off", READY, HUMAN, false, Decision.PVP_OFF),
        Arguments.of(
            "a human holding a crucifix",
            READY,
            new Target(true, false, true, true, true),
            true,
            Decision.WARDED),
        Arguments.of(
            "a crucifix wards even with PvP off",
            READY,
            new Target(true, false, true, true, true),
            false,
            Decision.WARDED),
        Arguments.of(
            "a downed vampire is helpless before the crucifix matters",
            new Biter(true, true, true, true),
            new Target(true, false, true, true, true),
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
  @DisplayName("only a bite drinks, and a refused bite still stops other interactions")
  void claims(final Decision decision) {
    assertEquals(decision == Decision.BITE, decision.bites());
    assertEquals(
        decision == Decision.BITE
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

  @ParameterizedTest(name = "human {0}, allowed {1}, first {2}, roll {3} of {4}%: {5}")
  @CsvSource({
    "true, true, true, 99, 100, true",
    "true, true, true, 50, 50, false",
    "true, true, true, 49, 50, true",
    "false, true, true, 0, 100, false",
    "true, false, true, 0, 100, false",
    "true, true, false, 0, 100, false"
  })
  @DisplayName("a bite starts sickness only in a human, when allowed, once, and by chance")
  void startsSickness(
      final boolean human,
      final boolean allowed,
      final boolean first,
      final int roll,
      final int chance,
      final boolean starts) {
    assertEquals(starts, BiteRules.startsSickness(human, allowed, first, roll, chance));
  }

  @ParameterizedTest(name = "{0}% with {1} resistance: {2}%")
  @CsvSource({
    "100, 0.0, 100",
    "100, 0.35, 65",
    "100, 0.7, 30",
    "100, 1.0, 0",
    "100, 1.5, 0",
    "100, -0.5, 100",
    "50, 0.35, 33",
    "0, 0.35, 0"
  })
  @DisplayName("bite resistance lowers the chance a bite starts the sickness")
  void resistedChance(final int chance, final double resistance, final int left) {
    assertEquals(left, BiteRules.resistedChance(chance, resistance));
  }
}
