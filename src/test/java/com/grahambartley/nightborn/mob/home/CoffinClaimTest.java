package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinClaimTest {

  static final UUID ME = UUID.fromString("00000000-0000-0000-0000-000000000001");
  static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

  static Stream<Arguments> kept() {
    return Stream.of(
        Arguments.of(true, ME, true),
        Arguments.of(true, OTHER, false),
        Arguments.of(true, null, false),
        Arguments.of(false, ME, false));
  }

  @ParameterizedTest(name = "coffin head {0}, owner {1}: kept {2}")
  @MethodSource("kept")
  @DisplayName("a vampire keeps its coffin while it still stands and still records it")
  void keeps(final boolean head, final UUID owner, final boolean kept) {
    assertEquals(kept, CoffinClaim.keeps(head, Optional.ofNullable(owner), ME));
  }

  static Stream<Arguments> claimable() {
    return Stream.of(
        Arguments.of(true, null, true),
        Arguments.of(true, OTHER, false),
        Arguments.of(true, ME, false),
        Arguments.of(false, null, false));
  }

  @ParameterizedTest(name = "coffin head {0}, owner {1}: claimable {2}")
  @MethodSource("claimable")
  @DisplayName("only a coffin no one owns can be claimed")
  void mayClaim(final boolean head, final UUID owner, final boolean claimable) {
    assertEquals(claimable, CoffinClaim.mayClaim(head, Optional.ofNullable(owner)));
  }
}
