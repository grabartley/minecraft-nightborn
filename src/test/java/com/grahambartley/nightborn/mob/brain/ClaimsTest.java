package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClaimsTest {

  private static final UUID ALLY = new UUID(0, 1);
  private static final UUID HEALER = new UUID(0, 2);
  private static final UUID OTHER_HEALER = new UUID(0, 3);

  @ParameterizedTest(name = "asked by the claimer {0} at tick {1}: claimed {2}")
  @CsvSource({
    "false, 100, true",
    "false, 111, true",
    "false, 112, false",
    "true, 100, false",
    "true, 111, false"
  })
  @DisplayName(
      "an ally claimed for a tell is off limits to every other mob until the tell lands, and"
          + " never to the one that claimed it")
  void claimLastsTheTell(final boolean claimer, final long now, final boolean claimed) {
    final Claims claims = new Claims();
    claims.claim(ALLY, HEALER, 112, 100);
    assertEquals(claimed, claims.claimedByOther(ALLY, claimer ? HEALER : OTHER_HEALER, now));
  }

  @Test
  @DisplayName("an ally nobody claimed, or whose claims were cleared, is free to help")
  void unclaimed() {
    final Claims claims = new Claims();
    assertFalse(claims.claimedByOther(ALLY, OTHER_HEALER, 100));
    claims.claim(ALLY, HEALER, 200, 100);
    claims.clear();
    assertFalse(claims.claimedByOther(ALLY, OTHER_HEALER, 100));
  }

  @ParameterizedTest(name = "a second claim at tick {0} keeps {1}")
  @CsvSource({"105, 2", "112, 1", "300, 1"})
  @DisplayName("a claim that has run out is dropped when the next one is made")
  void prunesOldClaims(final long now, final int kept) {
    final Claims claims = new Claims();
    claims.claim(ALLY, HEALER, 112, 100);
    claims.claim(new UUID(0, 9), OTHER_HEALER, now + 12, now);
    assertEquals(kept, claims.size());
  }
}
