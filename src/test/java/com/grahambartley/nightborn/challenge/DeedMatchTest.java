package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeedMatchTest {

  static final Identifier STAKED = Identifier.of("nightborn", "vampire_staked");
  static final Identifier FED = Identifier.of("nightborn", "fed");

  @ParameterizedTest(name = "{0} in village {1} at stage {2} matches: {3}")
  @CsvSource({
    "nightborn:vampire_staked, true, 2, true",
    "nightborn:vampire_staked, true, 3, true",
    "nightborn:vampire_staked, true, 1, false",
    "nightborn:vampire_staked, false, 2, false",
    "nightborn:fed, true, 2, false"
  })
  void needsTheKindEveryFactAndEveryFloor(
      final String kind, final boolean village, final int stage, final boolean matches) {
    final DeedMatch match =
        new DeedMatch(STAKED, Map.of("in_village", "true"), Map.of("stage_rank", 2));
    final Deed deed =
        Deed.of(Identifier.of(kind)).with("in_village", village).with("stage_rank", stage);
    assertEquals(matches, match.matches(deed));
  }

  @ParameterizedTest(name = "a deed missing {0} never matches")
  @CsvSource({"in_village", "stage_rank"})
  void aMissingFactNeverMatches(final String missing) {
    final DeedMatch match =
        new DeedMatch(STAKED, Map.of("in_village", "true"), Map.of("stage_rank", 0));
    Deed deed = Deed.of(STAKED);
    if (!missing.equals("in_village")) {
      deed = deed.with("in_village", true);
    }
    if (!missing.equals("stage_rank")) {
      deed = deed.with("stage_rank", 4);
    }
    assertEquals(false, match.matches(deed));
  }

  @ParameterizedTest(name = "a bare match takes any {0}")
  @CsvSource({"true", "false"})
  void aBareMatchTakesAnyFacts(final boolean seen) {
    assertEquals(true, DeedMatch.of(FED).matches(Deed.of(FED).with("seen", seen)));
  }
}
