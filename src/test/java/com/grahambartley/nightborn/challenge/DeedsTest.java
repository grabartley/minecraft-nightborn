package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeedsTest {

  private static Trial trialFor(final Side side) {
    return new Trial(
        Text.literal("t"),
        Text.literal("d"),
        side,
        new DeedGoal(
            DeedMatch.of(DeedKinds.FED),
            1,
            DeedGoal.Measure.TOTAL,
            Optional.empty(),
            DeedGoal.Within.ANY_TIME,
            Optional.empty()),
        Optional.empty(),
        List.of(),
        TrialSize.SMALL,
        Optional.empty());
  }

  @ParameterizedTest(name = "{0} trial finished -> {1} of its side")
  @CsvSource({"VAMPIRE, 2", "HUNTER, 1"})
  void aFinishedTrialReportsHowManyOfItsSideAreDone(final Side side, final int distinct) {
    final Identifier bite = Identifier.of("nightborn", "bite");
    final Identifier drink = Identifier.of("nightborn", "drink");
    final Identifier stake = Identifier.of("nightborn", "stake");
    final ChallengeRecord record =
        ChallengeRecord.EMPTY.finishing(bite).finishing(drink).finishing(stake);
    final Map<Identifier, Trial> trials =
        Map.of(
            bite, trialFor(Side.VAMPIRE),
            drink, trialFor(Side.VAMPIRE),
            stake, trialFor(Side.HUNTER));
    final Identifier finished = side == Side.VAMPIRE ? bite : stake;
    final Deed deed = Deeds.completed(finished, side, record, trials);
    assertEquals(DeedKinds.TRIAL_COMPLETED, deed.kind());
    assertEquals(Optional.of(finished.toString()), deed.fact(DeedKinds.TRIAL));
    assertEquals(Optional.of(side.asString()), deed.fact(DeedKinds.SIDE));
    assertEquals(distinct, deed.number(DeedKinds.DISTINCT).getAsInt());
  }
}
