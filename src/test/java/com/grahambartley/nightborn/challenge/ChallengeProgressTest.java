package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ChallengeProgressTest {

  static final Identifier KILL = Identifier.of("nightborn", "finisher_kill");
  static final Identifier STAKED = Identifier.of("nightborn", "staked");
  static final Identifier REACHED = Identifier.of("nightborn", "stage_reached");
  static final Identifier LAST_DROP = Identifier.of("nightborn", "the_last_drop");
  static final Identifier NEVER_STAKED = Identifier.of("nightborn", "never_staked");

  static DeedGoal goal(final Identifier deed, final int count) {
    return new DeedGoal(
        DeedMatch.of(deed),
        count,
        DeedGoal.Measure.TOTAL,
        Optional.empty(),
        DeedGoal.Within.ANY_TIME,
        Optional.empty());
  }

  static final Trial TRIAL =
      new Trial(
          Text.literal("t"),
          Text.literal("d"),
          Side.VAMPIRE,
          goal(KILL, 2),
          Optional.empty(),
          List.of(),
          TrialSize.MEDIUM,
          Optional.empty());

  static final Feat FEAT =
      new Feat(
          Text.literal("f"),
          Text.literal("d"),
          Text.literal("the Undying"),
          Side.VAMPIRE,
          goal(REACHED, 1),
          Optional.of(DeedMatch.of(STAKED)),
          Identifier.of("nightborn", "cracked_stake_dome"),
          Optional.empty());

  static ChallengeProgress.Context context(final Side side) {
    return new ChallengeProgress.Context(
        Map.of(LAST_DROP, TRIAL),
        Map.of(NEVER_STAKED, FEAT),
        Optional.ofNullable(side),
        OptionalLong.empty());
  }

  static ChallengeRecord offering(final boolean taken, final boolean finishedBefore) {
    final ChallengeRecord record =
        ChallengeRecord.EMPTY.withOffer(
            new TrialOffer(
                Optional.of(Side.VAMPIRE), 1L, List.of(OfferedTrial.fresh(LAST_DROP, taken))));
    return finishedBefore ? record.finishing(LAST_DROP) : record;
  }

  @ParameterizedTest(name = "{0} kills, taken {1}, finished before {2}: done {3}, first {4}")
  @CsvSource({
    "1, true, false, 0, false",
    "2, true, false, 1, true",
    "2, true, true, 1, false",
    "5, true, false, 1, true",
    "2, false, false, 0, false"
  })
  void aTrialPaysOnceWhenItsCountIsMet(
      final int kills,
      final boolean taken,
      final boolean finishedBefore,
      final int paid,
      final boolean firstTime) {
    ChallengeRecord record = offering(taken, finishedBefore);
    int done = 0;
    boolean first = false;
    for (int kill = 0; kill < kills; kill++) {
      final ChallengeProgress.Outcome outcome =
          ChallengeProgress.apply(record, Deed.of(KILL), context(Side.VAMPIRE));
      record = outcome.record();
      done += outcome.trials().size();
      first |= outcome.trials().stream().anyMatch(ChallengeProgress.TrialDone::firstTime);
    }
    assertEquals(paid, done);
    assertEquals(firstTime, first);
  }

  @ParameterizedTest(name = "a {0} never counts a vampire's trial")
  @CsvSource({"HUNTER", "''"})
  void aTrialCountsOnlyOnItsOffersSide(final String side) {
    final ChallengeRecord record = offering(true, false);
    final ChallengeProgress.Outcome outcome =
        ChallengeProgress.apply(
            record, Deed.of(KILL), context(side.isEmpty() ? null : Side.valueOf(side)));
    assertEquals(record, outcome.record());
  }

  @ParameterizedTest(name = "staked first {0}: the feat is earned {1}")
  @CsvSource({"false, 1", "true, 0"})
  void aSpoiledFeatCanNeverBeEarned(final boolean stakedFirst, final int earned) {
    ChallengeRecord record = ChallengeRecord.EMPTY;
    if (stakedFirst) {
      record = ChallengeProgress.apply(record, Deed.of(STAKED), context(Side.VAMPIRE)).record();
    }
    final ChallengeProgress.Outcome outcome =
        ChallengeProgress.apply(record, Deed.of(REACHED), context(Side.VAMPIRE));
    assertEquals(earned, outcome.feats().size());
    assertEquals(earned == 1, outcome.record().feats().earned().getOrDefault(NEVER_STAKED, false));
  }

  @ParameterizedTest(name = "a feat is earned once over {0} deeds")
  @CsvSource({"1", "3"})
  void aFeatIsEarnedOnlyOnce(final int deeds) {
    ChallengeRecord record = ChallengeRecord.EMPTY;
    int earned = 0;
    for (int i = 0; i < deeds; i++) {
      final ChallengeProgress.Outcome outcome =
          ChallengeProgress.apply(record, Deed.of(REACHED), context(Side.VAMPIRE));
      record = outcome.record();
      earned += outcome.feats().size();
    }
    assertEquals(1, earned);
  }
}
