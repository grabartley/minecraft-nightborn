package com.grahambartley.nightborn.faction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class OathTest {

  @ParameterizedTest(name = "proven {0}, sworn {1}, vampire {2} -> hunter {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, false",
    "true, true, false, true",
    "false, true, false, true",
    "true, true, true, false"
  })
  @DisplayName("a hunter is a human who took the oath")
  void hunter(
      final boolean proven, final boolean sworn, final boolean vampire, final boolean hunter) {
    assertEquals(hunter, new Oath(proven, sworn, 0, 0).hunter(vampire));
  }

  @ParameterizedTest(name = "proven {0}, sworn {1}, vampire {2} -> sided {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, false",
    "true, true, false, true",
    "false, false, true, true",
    "true, false, true, true"
  })
  @DisplayName("a player has chosen a side once turned or sworn in, never by proving alone")
  void sided(
      final boolean proven, final boolean sworn, final boolean vampire, final boolean sided) {
    assertEquals(sided, new Oath(proven, sworn, 0, 0).sided(vampire));
  }

  @ParameterizedTest(name = "proven {0}, sworn {1}, vampire {2} -> may swear {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, true",
    "true, true, false, false",
    "true, false, true, false"
  })
  @DisplayName("only a proven human who is not yet a hunter may be sworn in")
  void maySwear(
      final boolean proven, final boolean sworn, final boolean vampire, final boolean may) {
    assertEquals(may, new Oath(proven, sworn, 0, 0).maySwear(vampire));
  }

  static Stream<Arguments> steps() {
    return Stream.of(
        Arguments.of(Oath.NONE, new Oath(true, false, 0, 0), new Oath(true, true, 0, 0)),
        Arguments.of(
            new Oath(true, false, 0, 0), new Oath(true, false, 0, 0), new Oath(true, true, 0, 0)),
        Arguments.of(
            new Oath(false, true, 0, 0), new Oath(true, true, 0, 0), new Oath(true, true, 0, 0)));
  }

  @ParameterizedTest(name = "{0}: proved {1}, sworn {2}")
  @MethodSource("steps")
  @DisplayName("proving and swearing only ever move forward")
  void steps(final Oath from, final Oath proved, final Oath sworn) {
    assertEquals(proved, from.prove());
    assertEquals(sworn, from.swear());
  }

  static Stream<Oath> oaths() {
    return Stream.of(
        Oath.NONE,
        new Oath(true, false, 0, 0),
        new Oath(true, true, 0, 0),
        new Oath(true, true, 2, 0b1110));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("oaths")
  @DisplayName("an oath saves and loads unchanged")
  void roundTrips(final Oath oath) {
    final var json = Oath.CODEC.encodeStart(JsonOps.INSTANCE, oath).getOrThrow();
    assertEquals(oath, Oath.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("oaths")
  @DisplayName("an oath reaches its own player's client unchanged")
  void packetRoundTrips(final Oath oath) {
    final ByteBuf buf = Unpooled.buffer();
    Oath.PACKET_CODEC.encode(buf, oath);
    assertEquals(oath, Oath.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "rank {0} is kept as {1}")
  @CsvSource({"-3, 0", "0, 0", "3, 3", "15, 15", "99, 15"})
  @DisplayName("a rank stays within the ladder's bounds")
  void clampsRank(final int given, final int kept) {
    assertEquals(kept, new Oath(true, true, given, 0).rank());
  }

  @ParameterizedTest(name = "deed for rank {0} recorded {1}")
  @CsvSource({"1, true", "3, true", "-1, false", "16, false"})
  @DisplayName("a deed is recorded once and keeps the rank and the oath")
  void recordsDeeds(final int forRank, final boolean recorded) {
    final Oath before = new Oath(true, true, 1, 0);
    final Oath after = before.withDeed(forRank).withDeed(forRank);
    assertEquals(recorded, after.deedDone(forRank));
    assertEquals(1, after.rank());
    assertTrue(after.sworn());
  }

  @ParameterizedTest(name = "rank {0}")
  @CsvSource({"0", "2", "3"})
  @DisplayName("proving, swearing and rising keep the rank and deeds already held")
  void keepsStanding(final int rank) {
    final Oath held = new Oath(true, false, rank, 0b100);
    assertEquals(rank, held.swear().rank());
    assertEquals(0b100, held.prove().deeds());
    assertEquals(new Oath(true, false, 3, 0b100), held.withRank(3));
  }
}
