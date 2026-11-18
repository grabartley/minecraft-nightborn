package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrialOfferTest {

  static final Identifier A = Identifier.of("nightborn", "bat_season");
  static final Identifier B = Identifier.of("nightborn", "watchman");

  @ParameterizedTest(name = "an offer for {0} in period {1} is current for {2} in {3}: {4}")
  @CsvSource({
    "HUNTER, 3, HUNTER, 3, true",
    "HUNTER, 3, HUNTER, 4, false",
    "HUNTER, 3, VAMPIRE, 3, false",
    "HUNTER, 3, , 3, false"
  })
  void isCurrentOnlyForTheSameSideAndPeriod(
      final Side side,
      final long period,
      final Side now,
      final long nowPeriod,
      final boolean current) {
    final TrialOffer offer = new TrialOffer(Optional.of(side), period, List.of());
    assertEquals(current, offer.current(Optional.ofNullable(now), nowPeriod));
  }

  @ParameterizedTest(name = "updating {0} changes only it")
  @CsvSource({"nightborn:bat_season", "nightborn:watchman"})
  void updatesOneTrialAndKeepsTheRest(final String id) {
    final TrialOffer offer =
        new TrialOffer(
            Optional.of(Side.HUNTER),
            1L,
            List.of(OfferedTrial.fresh(A, false), OfferedTrial.fresh(B, false)));
    final TrialOffer taken = offer.updating(Identifier.of(id), OfferedTrial::taking);
    assertEquals(true, taken.find(Identifier.of(id)).orElseThrow().taken());
    final Identifier other = Identifier.of(id).equals(A) ? B : A;
    assertEquals(false, taken.find(other).orElseThrow().taken());
    final ByteBuf buf = Unpooled.buffer();
    TrialOffer.PACKET_CODEC.encode(buf, taken);
    assertEquals(taken, TrialOffer.PACKET_CODEC.decode(buf));
  }
}
