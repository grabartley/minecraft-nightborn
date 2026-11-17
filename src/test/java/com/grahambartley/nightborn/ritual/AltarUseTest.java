package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class AltarUseTest {

  private static final AltarUse.Hand EMPTY = new AltarUse.Hand(true, false, false, false);
  private static final AltarUse.Hand SNEAKING = new AltarUse.Hand(true, false, false, true);
  private static final AltarUse.Hand NEW_RELIC = new AltarUse.Hand(false, true, false, false);
  private static final AltarUse.Hand SAME_RELIC = new AltarUse.Hand(false, true, true, false);
  private static final AltarUse.Hand OTHER_ITEM = new AltarUse.Hand(false, false, false, false);

  private static AltarUse.Altar altar(final boolean active, final int placed) {
    return new AltarUse.Altar(active, placed, 4);
  }

  static Stream<Arguments> uses() {
    return Stream.of(
        Arguments.of("a new relic goes on", altar(false, 1), NEW_RELIC, true, true, AltarUse.PLACE),
        Arguments.of(
            "a vampire may place one", altar(false, 0), NEW_RELIC, false, false, AltarUse.PLACE),
        Arguments.of(
            "a second of the same does not",
            altar(false, 1),
            SAME_RELIC,
            true,
            true,
            AltarUse.PASS),
        Arguments.of(
            "another item does nothing", altar(false, 4), OTHER_ITEM, true, true, AltarUse.PASS),
        Arguments.of(
            "an empty altar gives nothing", altar(false, 0), EMPTY, true, true, AltarUse.PASS),
        Arguments.of(
            "a part set comes back off", altar(false, 3), EMPTY, true, true, AltarUse.TAKE),
        Arguments.of(
            "sneaking takes from a full set", altar(false, 4), SNEAKING, true, true, AltarUse.TAKE),
        Arguments.of(
            "a human at night starts it", altar(false, 4), EMPTY, true, true, AltarUse.START),
        Arguments.of("day refuses", altar(false, 4), EMPTY, true, false, AltarUse.NOT_NIGHT),
        Arguments.of(
            "a vampire is refused", altar(false, 4), EMPTY, false, true, AltarUse.NOT_HUMAN),
        Arguments.of(
            "a vampire by day is refused as a vampire",
            altar(false, 4),
            EMPTY,
            false,
            false,
            AltarUse.NOT_HUMAN),
        Arguments.of(
            "a running ritual takes no relic",
            altar(true, 4),
            NEW_RELIC,
            true,
            true,
            AltarUse.BUSY),
        Arguments.of(
            "a running ritual gives none back",
            altar(true, 4),
            SNEAKING,
            true,
            true,
            AltarUse.BUSY),
        Arguments.of(
            "no relics defined never starts",
            new AltarUse.Altar(false, 1, 0),
            EMPTY,
            true,
            true,
            AltarUse.TAKE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("uses")
  @DisplayName("using the altar places, takes, starts or refuses")
  void decide(
      final String name,
      final AltarUse.Altar altar,
      final AltarUse.Hand hand,
      final boolean human,
      final boolean night,
      final AltarUse use) {
    assertEquals(use, AltarUse.decide(altar, hand, human, night));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(AltarUse.class)
  @DisplayName("only the refusals carry a message")
  void refusal(final AltarUse use) {
    final Optional<String> expected =
        switch (use) {
          case NOT_HUMAN -> Optional.of("nightborn.ritual.not_human");
          case NOT_NIGHT -> Optional.of("nightborn.ritual.not_night");
          default -> Optional.empty();
        };
    assertEquals(expected, use.refusal());
  }
}
