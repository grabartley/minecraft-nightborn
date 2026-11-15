package com.grahambartley.nightborn.hunter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.downed.DownedState.Phase;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HuntRewardsTest {

  static final UUID HUNTER = UUID.fromString("00000000-0000-0000-0000-000000000047");
  static final UUID STAKER = UUID.fromString("00000000-0000-0000-0000-000000000053");

  static Stream<Arguments> cases() {
    final Optional<UUID> none = Optional.empty();
    final DownedState downed = new DownedState(Phase.DOWNED, 100, Optional.of(HUNTER));
    final DownedState escaping = new DownedState(Phase.ESCAPING, 100, Optional.of(HUNTER));
    final DownedState byNobody = new DownedState(Phase.DOWNED, 100);
    return Stream.of(
        Arguments.of("downed, then the sun", true, downed, true, none, Optional.of(HUNTER)),
        Arguments.of("escaping, then the sun", true, escaping, true, none, Optional.of(HUNTER)),
        Arguments.of("downed, then something else", true, downed, false, none, Optional.empty()),
        Arguments.of("downed by no human", true, byNobody, true, none, Optional.empty()),
        Arguments.of("never downed", true, null, true, none, Optional.empty()),
        Arguments.of(
            "staked while downed", true, downed, false, Optional.of(STAKER), Optional.of(STAKER)),
        Arguments.of(
            "staked standing", true, null, false, Optional.of(STAKER), Optional.of(STAKER)),
        Arguments.of("staked a non-vampire", false, null, false, Optional.of(STAKER), none),
        Arguments.of("sun on a non-vampire", false, downed, true, none, none));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  @DisplayName("only a vampire pays: its staker, or the human who downed it when the sun ends it")
  void hunter(
      final String name,
      final boolean vampire,
      final DownedState state,
      final boolean sunKill,
      final Optional<UUID> staker,
      final Optional<UUID> paid) {
    assertEquals(paid, HuntRewards.hunter(vampire, state, sunKill, staker));
  }
}
