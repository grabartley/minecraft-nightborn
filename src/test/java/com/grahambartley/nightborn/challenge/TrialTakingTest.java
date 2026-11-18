package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrialTakingTest {

  static final Identifier A = Identifier.of("nightborn", "watchman");
  static final Identifier B = Identifier.of("nightborn", "bat_season");

  static ChallengeRecord hunterOffer() {
    return ChallengeRecord.EMPTY.withOffer(
        new TrialOffer(
            Optional.of(Side.HUNTER),
            1L,
            List.of(OfferedTrial.fresh(A, false), OfferedTrial.fresh(B, true))));
  }

  @ParameterizedTest(name = "a {0} asking for trial {1} takes it: {2}")
  @CsvSource({
    "HUNTER, 0, true",
    "HUNTER, 1, false",
    "HUNTER, 2, false",
    "HUNTER, -1, false",
    "VAMPIRE, 0, false",
    ", 0, false"
  })
  void onlyAHunterTakesAnUntakenTrialOnOffer(
      final Side side, final int index, final boolean takes) {
    final Optional<TrialTaking.Taken> taken =
        TrialTaking.take(hunterOffer(), Optional.ofNullable(side), index);
    assertEquals(takes, taken.isPresent());
    taken.ifPresent(
        t -> {
          assertEquals(A, t.trial());
          assertEquals(true, t.record().offer().find(A).orElseThrow().taken());
        });
  }
}
