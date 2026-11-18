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

  static ChallengeRecord withOffer(final TrialOffer offer) {
    return ChallengeRecord.EMPTY.withOffer(offer);
  }

  @ParameterizedTest(name = "a {0} draws with taken {1}")
  @CsvSource({"VAMPIRE, true", "HUNTER, false"})
  void aVampireHoldsItsTrialsAtOnceAndAHunterMustTakeThem(final Side side, final boolean taken) {
    final ChallengeRecord record =
        TrialDraw.renewed(ChallengeRecord.EMPTY, Optional.of(side), 2L, candidates(4, 0), 3, 11L)
            .orElseThrow();
    assertEquals(3, record.offer().trials().size());
    assertTrue(record.offer().trials().stream().allMatch(trial -> trial.taken() == taken));
    assertEquals(
        Optional.empty(),
        TrialDraw.renewed(record, Optional.of(side), 2L, candidates(4, 0), 3, 11L));
  }

  @ParameterizedTest(name = "a player with no side and an offer {0} is left with none")
  @CsvSource({"true", "false"})
  void aPlayerWithNoSideHasNoTrials(final boolean hadOne) {
    final ChallengeRecord before =
        hadOne
            ? TrialDraw.renewed(
                    ChallengeRecord.EMPTY, Optional.of(Side.HUNTER), 1L, candidates(3, 0), 3, 1L)
                .orElseThrow()
            : ChallengeRecord.EMPTY;
    final Optional<ChallengeRecord> after =
        TrialDraw.renewed(before, Optional.empty(), 1L, candidates(3, 0), 3, 1L);
    assertEquals(hadOne, after.isPresent());
    after.ifPresent(record -> assertEquals(TrialOffer.NONE, record.offer()));
  }

  @ParameterizedTest(name = "{0} to {1} and back in one period keeps the offer that was paid")
  @CsvSource({"VAMPIRE, HUNTER", "HUNTER, VAMPIRE", "VAMPIRE, ''"})
  void changingSideAndBackInOnePeriodRestoresTheSameOffer(final Side first, final String other) {
    final List<TrialDraw.Candidate> pool = candidates(17, 3);
    final ChallengeRecord drawn =
        TrialDraw.renewed(ChallengeRecord.EMPTY, Optional.of(first), 5L, pool, 3, 7L).orElseThrow();
    final Identifier paid = drawn.offer().trials().get(0).trial();
    final ChallengeRecord done =
        drawn
            .withOffer(
                drawn.offer().updating(paid, o -> new OfferedTrial(paid, true, true, Tally.EMPTY)))
            .finishing(paid);
    final Optional<Side> away =
        other.isEmpty() ? Optional.empty() : Optional.of(Side.valueOf(other));
    final ChallengeRecord switched = TrialDraw.renewed(done, away, 5L, pool, 3, 99L).orElseThrow();
    final List<TrialDraw.Candidate> poolLater =
        pool.stream()
            .map(
                c ->
                    new TrialDraw.Candidate(
                        c.trial(), c.finishedBefore() || c.trial().equals(paid)))
            .toList();
    final ChallengeRecord back =
        TrialDraw.renewed(switched, Optional.of(first), 5L, poolLater, 3, 7L).orElseThrow();
    assertEquals(done.offer(), back.offer());
  }

  @ParameterizedTest(name = "an offer kept from period {0} is dropped in period {1}")
  @CsvSource({"5, 6", "5, 9"})
  void anOfferFromAnEarlierPeriodIsNeverRestored(final long earlier, final long now) {
    final List<TrialDraw.Candidate> pool = candidates(20, 0);
    final ChallengeRecord vampire =
        TrialDraw.renewed(ChallengeRecord.EMPTY, Optional.of(Side.VAMPIRE), earlier, pool, 3, 1L)
            .orElseThrow();
    final ChallengeRecord hunter =
        TrialDraw.renewed(vampire, Optional.of(Side.HUNTER), earlier, pool, 3, 2L).orElseThrow();
    final ChallengeRecord later =
        TrialDraw.renewed(hunter, Optional.of(Side.VAMPIRE), now, pool, 3, 3L).orElseThrow();
    assertEquals(now, later.offer().period());
    assertEquals(List.of(), later.shelved());
  }
}
