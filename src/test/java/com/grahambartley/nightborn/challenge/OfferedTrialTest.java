package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OfferedTrialTest {

  static final Identifier WINGS = Identifier.of("nightborn", "wings");

  @ParameterizedTest(name = "taken {0}, done {1} counts: {2}")
  @CsvSource({"true, false, true", "false, false, false", "true, true, false"})
  void countsOnlyWhileTakenAndUnfinished(
      final boolean taken, final boolean done, final boolean counting) {
    final OfferedTrial offered = new OfferedTrial(WINGS, taken, done, Tally.EMPTY);
    assertEquals(counting, offered.counting());
    final ByteBuf buf = Unpooled.buffer();
    OfferedTrial.PACKET_CODEC.encode(buf, offered);
    assertEquals(offered, OfferedTrial.PACKET_CODEC.decode(buf));
    assertEquals(
        offered,
        OfferedTrial.CODEC
            .parse(
                JsonOps.INSTANCE,
                OfferedTrial.CODEC.encodeStart(JsonOps.INSTANCE, offered).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "taking a trial with {0} counted starts it from nothing")
  @CsvSource({"0", "4"})
  void takingStartsTheCountAfresh(final int counted) {
    final OfferedTrial offered =
        new OfferedTrial(WINGS, false, false, new Tally(counted, List.of(), 1L));
    assertEquals(new OfferedTrial(WINGS, true, false, Tally.EMPTY), offered.taking());
  }

  @ParameterizedTest(name = "once done it stays done ({0})")
  @CsvSource({"true", "false"})
  void stayDoneOnceDone(final boolean finished) {
    final OfferedTrial done = new OfferedTrial(WINGS, true, true, Tally.EMPTY);
    assertEquals(true, done.counted(Tally.EMPTY, finished).done());
  }
}
