package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrialDrawTest {

  static List<TrialDraw.Candidate> candidates(final int fresh, final int again) {
    return IntStream.range(0, fresh + again)
        .mapToObj(i -> new TrialDraw.Candidate(Identifier.of("nightborn", "t" + i), i >= fresh))
        .toList();
  }

  @ParameterizedTest(name = "{0} unfinished and {1} finished, draw {2} with seed {3}")
  @CsvSource({"5, 5, 3, 1", "2, 5, 3, 7", "0, 4, 3, 9", "1, 0, 3, 2", "6, 0, 0, 5"})
  void drawsUnfinishedTrialsFirst(
      final int fresh, final int again, final int size, final long seed) {
    final List<TrialDraw.Candidate> pool = candidates(fresh, again);
    final List<Identifier> drawn = TrialDraw.draw(pool, size, seed);
    assertEquals(Math.min(size, fresh + again), drawn.size());
    final long unfinished =
        drawn.stream()
            .filter(id -> pool.stream().anyMatch(c -> c.trial().equals(id) && !c.finishedBefore()))
            .count();
    assertEquals(Math.min(fresh, drawn.size()), unfinished);
    assertEquals(drawn, TrialDraw.draw(pool, size, seed));
    assertEquals(drawn.size(), drawn.stream().distinct().count());
  }

  @ParameterizedTest(name = "a {0} draws with taken {1}")
  @CsvSource({"VAMPIRE, true", "HUNTER, false"})
  void aVampireHoldsItsTrialsAtOnceAndAHunterMustTakeThem(final Side side, final boolean taken) {
    final TrialOffer offer =
        TrialDraw.renewed(TrialOffer.NONE, Optional.of(side), 2L, candidates(4, 0), 3, 11L)
            .orElseThrow();
    assertEquals(3, offer.trials().size());
    assertTrue(offer.trials().stream().allMatch(trial -> trial.taken() == taken));
    assertEquals(
        Optional.empty(),
        TrialDraw.renewed(offer, Optional.of(side), 2L, candidates(4, 0), 3, 11L));
  }

  @ParameterizedTest(name = "a player with no side and offer {0} is left with none")
  @CsvSource({"true", "false"})
  void aPlayerWithNoSideHasNoTrials(final boolean hadOne) {
    final TrialOffer before =
        hadOne
            ? TrialDraw.renewed(
                    TrialOffer.NONE, Optional.of(Side.HUNTER), 1L, candidates(3, 0), 3, 1L)
                .orElseThrow()
            : TrialOffer.NONE;
    assertEquals(
        hadOne ? Optional.of(TrialOffer.NONE) : Optional.empty(),
        TrialDraw.renewed(before, Optional.empty(), 1L, candidates(3, 0), 3, 1L));
  }
}
