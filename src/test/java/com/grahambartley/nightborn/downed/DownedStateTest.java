package com.grahambartley.nightborn.downed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.downed.DownedState.Phase;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DownedStateTest {

  @ParameterizedTest(name = "{0} ticks = {1} s")
  @CsvSource({"600, 30", "599, 30", "581, 30", "580, 29", "1, 1", "0, 0"})
  void secondsLeftRoundsUp(final int ticks, final int seconds) {
    assertEquals(seconds, new DownedState(Phase.DOWNED, ticks).secondsLeft());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Phase.class)
  void savesAndLoads(final Phase phase) {
    final DownedState state = new DownedState(phase, 321);
    assertEquals(
        state,
        DownedState.CODEC
            .parse(
                JsonOps.INSTANCE,
                DownedState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow())
            .getOrThrow());
    assertEquals(phase == Phase.DOWNED, state.isDowned());
  }
}
