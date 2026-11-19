package com.grahambartley.nightborn.hold;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HoldStateTest {

  @ParameterizedTest(name = "{0} until {1}, now {2}: held {3}")
  @CsvSource({"PIN, 100, 50, true", "PIN, 100, 100, false", "STAGGER, 100, 99, true"})
  @DisplayName("a hold is active until its end")
  void active(final HoldKind kind, final long until, final long now, final boolean held) {
    assertEquals(held, new HoldState(Optional.of(kind), until, until, false).held(now));
  }

  @ParameterizedTest(name = "{0} until {1}, immune until {2}")
  @CsvSource({"PIN, 100, 160", "STAGGER, 5, 5"})
  @DisplayName("a hold survives a save and a sync unchanged")
  void roundTrips(final HoldKind kind, final long until, final long immune) {
    final HoldState state = new HoldState(Optional.of(kind), until, immune, false);
    assertEquals(
        state,
        HoldState.CODEC
            .parse(
                JsonOps.INSTANCE, HoldState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow())
            .getOrThrow());
    final var buf = Unpooled.buffer();
    HoldState.PACKET_CODEC.encode(buf, state);
    assertEquals(state, HoldState.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0} until {1}, opening {2}, now {3}: opened {4}")
  @CsvSource({
    "STAGGER, 100, true, 50, true",
    "STAGGER, 100, true, 100, false",
    "STAGGER, 100, false, 50, false",
    "PIN, 100, true, 50, false"
  })
  @DisplayName("a stagger from a riposte or a dodge leaves its target open while it lasts")
  void opened(
      final HoldKind kind,
      final long until,
      final boolean opening,
      final long now,
      final boolean opened) {
    assertEquals(opened, new HoldState(Optional.of(kind), until, until, opening).opened(now));
  }
}
