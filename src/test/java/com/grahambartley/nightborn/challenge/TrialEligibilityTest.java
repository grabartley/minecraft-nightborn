package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TrialEligibilityTest {

  static final Identifier LIVE = Identifier.of("nightborn", "fed");
  static final Identifier UNBUILT = Identifier.of("nightborn", "nemesis_ended");
  static final Identifier ADULT = Identifier.of("nightborn", "adult");
  static final Identifier BAT = Identifier.of("nightborn", "beast/bat_form");

  static Trial trial(final Identifier deed, final boolean gated, final boolean needsBat) {
    return new Trial(
        Text.literal("t"),
        Text.literal("d"),
        Side.VAMPIRE,
        new DeedGoal(
            DeedMatch.of(deed),
            1,
            DeedGoal.Measure.TOTAL,
            Optional.empty(),
            DeedGoal.Within.ANY_TIME,
            Optional.empty()),
        gated ? Optional.of(ADULT) : Optional.empty(),
        needsBat ? List.of(BAT) : List.of(),
        TrialSize.SMALL,
        Optional.empty());
  }

  @ParameterizedTest(name = "side {0}, rank {1}, owns bat {2}; live {3}, gated {4}, bat {5}: {6}")
  @CsvSource({
    "VAMPIRE, 0, false, true, false, false, true",
    "HUNTER, 3, true, true, false, false, false",
    ", 3, true, true, false, false, false",
    "VAMPIRE, 0, false, true, true, false, false",
    "VAMPIRE, 1, false, true, true, false, true",
    "VAMPIRE, 3, false, true, false, true, false",
    "VAMPIRE, 3, true, true, false, true, true",
    "VAMPIRE, 3, true, false, false, false, false"
  })
  void offersOnlyWhatThePlayerCanDoNow(
      final Side side,
      final int rank,
      final boolean ownsBat,
      final boolean live,
      final boolean gated,
      final boolean needsBat,
      final boolean open) {
    final TrialEligibility eligibility =
        new TrialEligibility(
            Optional.ofNullable(side),
            rank,
            gate -> gate.equals(ADULT) ? OptionalInt.of(1) : OptionalInt.empty(),
            skill -> ownsBat && skill.equals(BAT),
            Set.of(LIVE));
    assertEquals(open, eligibility.open(trial(live ? LIVE : UNBUILT, gated, needsBat)));
  }
}
