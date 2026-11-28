package com.grahambartley.nightborn.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BalanceReportTest {

  private static final Matchup ONE = Matchup.ALL.get(3);

  private static List<RoundResult> rounds(
      final int vampireWins,
      final int hunterWins,
      final int draws,
      final int ticks,
      final float vampireMelee) {
    final List<RoundResult> rounds = new ArrayList<>();
    final Map<Side, Tally.Blows> blows =
        Map.of(
            Side.VAMPIRES, new Tally.Blows(1, vampireMelee, 1, 1.0F),
            Side.HUNTERS, new Tally.Blows(1, 2.0F, 1, 1.0F));
    final Map<Side, Set<String>> tags =
        Map.of(Side.HUNTERS, Set.of("hunter kit nightborn:stake"), Side.VAMPIRES, Set.of());
    for (int i = 0; i < vampireWins; i++) {
      rounds.add(new RoundResult(ONE.key(), Optional.of(Side.VAMPIRES), ticks, blows, tags));
    }
    for (int i = 0; i < hunterWins; i++) {
      rounds.add(new RoundResult(ONE.key(), Optional.of(Side.HUNTERS), ticks, blows, tags));
    }
    for (int i = 0; i < draws; i++) {
      rounds.add(new RoundResult(ONE.key(), Optional.empty(), ticks, blows, tags));
    }
    return rounds;
  }

  @ParameterizedTest(name = "{0} v {1} with {2} draws over {3} ticks, melee {4}: passes {5}")
  @CsvSource({
    "10, 10, 0, 500, 3.0, true",
    "9, 11, 0, 500, 3.0, true",
    "8, 12, 0, 500, 3.0, false",
    "10, 10, 3, 500, 3.0, false",
    "10, 10, 0, 300, 3.0, false",
    "10, 10, 0, 500, 0.1, false"
  })
  void row(
      final int vampireWins,
      final int hunterWins,
      final int draws,
      final int ticks,
      final float vampireMelee,
      final boolean passes) {
    final BalanceReport report =
        new BalanceReport(
            List.of(ONE), rounds(vampireWins, hunterWins, draws, ticks, vampireMelee));
    final BalanceReport.Row row = report.rows().get(0);
    assertEquals(vampireWins + hunterWins + draws, row.rounds());
    assertEquals(draws, row.draws());
    assertEquals(passes, row.passes());
    assertTrue(report.markdown().contains(ONE.key()));
  }

  @ParameterizedTest(name = "a kit in {0} decided rounds winning {1} passes {2}, measured {3}")
  @CsvSource({"20, 12, true, true", "20, 13, false, true", "9, 9, true, false"})
  void tags(final int rounds, final int hunterWins, final boolean passes, final boolean measured) {
    final BalanceReport report =
        new BalanceReport(List.of(ONE), rounds(rounds - hunterWins, hunterWins, 2, 500, 3.0F));
    final BalanceReport.TagRow kit = report.tags().get(0);
    assertEquals("hunter kit nightborn:stake", kit.tag());
    assertEquals(rounds, kit.rounds());
    assertEquals(passes, kit.passes());
    assertEquals(measured, kit.measured());
  }
}
