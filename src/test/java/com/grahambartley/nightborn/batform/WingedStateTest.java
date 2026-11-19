package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WingedStateTest {

  @ParameterizedTest(name = "until {0}, now {1}, rig {3}: over {2}")
  @CsvSource({"400, 399, false, nightborn:night_terror", "400, 400, true, "})
  @DisplayName("the winged form lasts until its time and survives a sync with its rig")
  void over(final long until, final long now, final boolean over, final String rig) {
    final WingedState state =
        new WingedState(
            0L,
            until,
            0.5F,
            2.5F,
            Optional.ofNullable(rig).map(Identifier::of),
            List.of("holy_water"));
    assertEquals(over, state.over(now));
    final var buf = Unpooled.buffer();
    WingedState.PACKET_CODEC.encode(buf, state);
    assertEquals(state, WingedState.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"until\":400,\"damage_taken\":0.5,\"scale\":2.5}; 0; ",
        "{\"since\":10,\"until\":400,\"damage_taken\":0.5,\"scale\":3.0,\"rig\":\"nightborn:night_terror\"}; 10; nightborn:night_terror"
      })
  @DisplayName("a form saved before it had a start or a rig loads as begun long ago with the bat")
  void loadsOlderSaves(final String json, final long since, final String rig) {
    final WingedState state =
        WingedState.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(since, state.since());
    assertEquals(Optional.ofNullable(rig).map(Identifier::of), state.rig());
  }
}
