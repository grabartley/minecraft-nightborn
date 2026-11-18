package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class TallyTest {

  static Stream<Tally> tallies() {
    return Stream.of(
        Tally.EMPTY,
        new Tally(7, List.of(), 3L),
        new Tally(0, List.of("nightborn:cow", "nightborn:pig"), Tally.NO_NIGHT));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("tallies")
  void survivesSavingAndTheNetwork(final Tally tally) {
    assertEquals(
        tally,
        Tally.CODEC
            .parse(JsonOps.INSTANCE, Tally.CODEC.encodeStart(JsonOps.INSTANCE, tally).getOrThrow())
            .getOrThrow());
    final ByteBuf buf = Unpooled.buffer();
    Tally.PACKET_CODEC.encode(buf, tally);
    assertEquals(tally, Tally.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0} plus {1} is {2}")
  @CsvSource({"0, 1, 1", "5, 3, 8", "5, -3, 5", "2147483647, 1, 2147483647"})
  void addsAndNeverOverflows(final int total, final int amount, final int after) {
    assertEquals(after, new Tally(total, List.of(), 0L).plus(amount).total());
  }

  @ParameterizedTest(name = "best {0}, then {1}, keeps {2}")
  @CsvSource({"10, 4, 10", "10, 40, 40", "0, 0, 0"})
  void keepsTheMost(final int best, final int now, final int kept) {
    assertEquals(kept, new Tally(best, List.of(), 0L).atLeast(now).total());
  }

  @ParameterizedTest(name = "seeing {0} twice counts once")
  @CsvSource({"nightborn:cow", "minecraft:villager"})
  void seesEachValueOnce(final String value) {
    assertEquals(List.of(value), Tally.EMPTY.seeing(value).seeing(value).seen());
  }
}
