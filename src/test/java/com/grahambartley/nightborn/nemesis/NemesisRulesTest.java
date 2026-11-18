package com.grahambartley.nightborn.nemesis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NemesisRulesTest {

  static final UUID QUARRY = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

  static NemesisKind kind(
      final boolean ages, final float boost, final int maxBoosts, final float chance) {
    return new NemesisKind(
        Identifier.of("nightborn", "hunter"),
        Identifier.of("nightborn", "broken_stake"),
        Identifier.of("nightborn", "nemesis_epithets"),
        chance,
        ages,
        boost,
        maxBoosts,
        5,
        2,
        30,
        NemesisLines.NONE);
  }

  static Optional<UUID> who(final String name) {
    return switch (name) {
      case "quarry" -> Optional.of(QUARRY);
      case "other" -> Optional.of(OTHER);
      default -> Optional.empty();
    };
  }

  @ParameterizedTest(name = "named {0}, boss {1}, sealed {2}, already {3}, holds {4} of {5}: {6}")
  @CsvSource({
    "true, false, false, false, 0, 3, true",
    "true, false, false, false, 2, 3, true",
    "true, false, false, false, 3, 3, false",
    "true, false, false, false, 4, 3, false",
    "true, false, false, false, 0, 0, false",
    "false, false, false, false, 0, 3, false",
    "true, true, false, false, 0, 3, false",
    "true, false, true, false, 0, 3, false",
    "true, false, false, true, 0, 3, false"
  })
  @DisplayName("only a named mob that leaves home becomes a nemesis, and never a fourth")
  void mayBecome(
      final boolean named,
      final boolean boss,
      final boolean sealed,
      final boolean already,
      final int held,
      final int limit,
      final boolean becomes) {
    assertEquals(becomes, NemesisRules.mayBecome(named, boss, sealed, already, held, limit));
  }

  @ParameterizedTest(name = "vampire mob {0}, household {1}, vampire player {2}, struck {3}: {4}")
  @CsvSource({
    "false, false, true, false, true",
    "false, false, true, true, true",
    "false, false, false, true, false",
    "true, true, true, true, true",
    "true, true, true, false, false",
    "true, false, true, true, false",
    "true, true, false, true, false"
  })
  @DisplayName("a hunter outlasts a vampire player, and a household vampire one who struck it")
  void outlasted(
      final boolean vampireMob,
      final boolean household,
      final boolean vampirePlayer,
      final boolean struck,
      final boolean grudge) {
    assertEquals(grudge, NemesisRules.outlasted(vampireMob, household, vampirePlayer, struck));
  }

  @ParameterizedTest(name = "now {0}, last blow {1}, ends after {2}: {3}")
  @CsvSource({
    "100, 100, 600, false",
    "699, 100, 600, false",
    "700, 100, 600, true",
    "9000, 0, 20, true"
  })
  @DisplayName("a fight is over once no blow has landed for a while")
  void fightOver(final long now, final long last, final int end, final boolean over) {
    assertEquals(over, NemesisRules.fightOver(now, last, end));
  }

  @ParameterizedTest(name = "night {0}, {1} to {2} nights, roll {3}: night {4}")
  @CsvSource({
    "10, 5, 10, 0, 15",
    "10, 5, 10, 5, 20",
    "10, 5, 10, 6, 15",
    "10, 5, 10, -1, 20",
    "0, 10, 5, 3, 10",
    "0, 0, 0, 7, 1"
  })
  @DisplayName("a nemesis picks a night 5 to 10 nights ahead")
  void nextVisit(
      final long night, final int fewest, final int most, final int roll, final long next) {
    assertEquals(next, NemesisRules.nextVisit(night, fewest, most, roll));
  }

  @ParameterizedTest(name = "night {0}, due {1}, dark {2}, overworld {3}, sky {4}: {5}")
  @CsvSource({
    "5, 5, true, true, true, true",
    "6, 5, true, true, true, true",
    "4, 5, true, true, true, false",
    "5, 5, false, true, true, false",
    "5, 5, true, false, true, false",
    "5, 5, true, true, false, false"
  })
  @DisplayName("a nemesis comes on its night, in the dark, to a quarry on the surface")
  void visitDue(
      final long night,
      final long due,
      final boolean dark,
      final boolean overworld,
      final boolean sky,
      final boolean comes) {
    assertEquals(comes, NemesisRules.visitDue(night, due, dark, overworld, sky));
  }

  @ParameterizedTest(name = "ages {0}, boost {1} up to {2}, taken {3}, can age {4}, roll {5}")
  @CsvSource({
    "true, 0, 0, 0, true, 0.2, true, false, true",
    "true, 0, 0, 5, false, 0.7, false, false, false",
    "false, 0.25, 3, 0, false, 0.5, false, true, false",
    "false, 0.25, 3, 2, false, 0.49, false, true, true",
    "false, 0.25, 3, 3, false, 0.1, false, false, true"
  })
  @DisplayName("a vampire ages a stage, a hunter takes a better kit three times, and half retitle")
  void growth(
      final boolean ages,
      final float boost,
      final int maxBoosts,
      final int taken,
      final boolean canAge,
      final double roll,
      final boolean aged,
      final boolean boosted,
      final boolean retitled) {
    assertEquals(
        new NemesisRules.Growth(aged, boosted, retitled),
        NemesisRules.growth(kind(ages, boost, maxBoosts, 0.5F), taken, canAge, roll));
  }

  @ParameterizedTest(name = "{0} boosts: {1}")
  @CsvSource({"0, 0", "1, 0.25", "3, 0.75", "5, 0.75", "-1, 0"})
  @DisplayName("each growth adds a quarter to a hunter's health and damage, up to three")
  void boost(final int boosts, final double value) {
    assertEquals(value, NemesisRules.boost(kind(false, 0.25F, 3, 0.5F), boosts), 1e-6);
  }

  @ParameterizedTest(name = "from {0}: {1}")
  @CsvSource({
    "fledgling, adult",
    "adult, elder",
    "elder, ancient",
    "ancient, ''",
    "lord, ''",
    "none, ''"
  })
  @DisplayName("a vampire climbs one stage a growth and stops at ancient")
  void nextRung(final String current, final String next) {
    final List<NemesisRules.Rung> rungs =
        List.of(
            new NemesisRules.Rung("eldest", 5, 0),
            new NemesisRules.Rung("ancient", 3, 7),
            new NemesisRules.Rung("fledgling", 0, 40),
            new NemesisRules.Rung("lord", 4, 0),
            new NemesisRules.Rung("elder", 2, 18),
            new NemesisRules.Rung("adult", 1, 35));
    assertEquals(
        next.isEmpty() ? Optional.empty() : Optional.of(next),
        NemesisRules.nextRung(rungs, current));
  }

  @ParameterizedTest(name = "{0} at {1} times: {2}")
  @CsvSource({"300, 5, 1500", "0, 5, 0", "300, 0, 0", "-10, 5, 0"})
  @DisplayName("ending a nemesis pays five times what its kind pays")
  void reward(final int base, final int times, final int paid) {
    assertEquals(paid, NemesisRules.reward(base, times));
  }

  @ParameterizedTest(name = "up to {0}, roll {1}: {2}")
  @CsvSource({"2, 0, 0", "2, 1, 1", "2, 2, 2", "2, 3, 0", "2, -1, 2", "0, 5, 0"})
  @DisplayName("a nemesis comes alone or with one or two of its own")
  void companions(final int most, final int roll, final int count) {
    assertEquals(count, NemesisRules.companions(most, roll));
  }

  @ParameterizedTest(name = "attacker {0}, sun {1}, downed by {2}: ended by quarry {3}")
  @CsvSource({
    "quarry, false, none, true",
    "other, false, quarry, false",
    "none, true, quarry, true",
    "none, false, quarry, false",
    "none, true, other, false",
    "none, true, none, false"
  })
  @DisplayName("the quarry ends it by its own hand, or by downing it for the sun")
  void endedByQuarry(
      final String attacker, final boolean sun, final String downedBy, final boolean ended) {
    assertEquals(
        ended,
        NemesisRules.endedByQuarry(NemesisRules.ender(who(attacker), sun, who(downedBy)), QUARRY));
  }

  @ParameterizedTest(name = "pool {0}, current {1}, roll {2}: {3}")
  @CsvSource({
    "the Unbowed|the Scarred, the Unbowed, 0, the Scarred",
    "the Unbowed|the Scarred, the Unbowed, 7, the Scarred",
    "the Unbowed|the Scarred, '', 1, the Scarred",
    "the Unbowed, the Unbowed, 3, ''",
    "'', '', 0, ''"
  })
  @DisplayName("a new epithet is never the one it already bears")
  void newEpithet(final String pool, final String current, final int roll, final String next) {
    final List<String> names = pool.isEmpty() ? List.of() : Arrays.asList(pool.split("\\|"));
    assertEquals(
        next.isEmpty() ? Optional.empty() : Optional.of(next),
        NemesisRules.newEpithet(names, current, roll));
  }
}
