package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RestlessDeadEntityTest {

  @ParameterizedTest(name = "bound {0}, ticks left {1}: returns to the ground {2}")
  @CsvSource({"true, 10, false", "true, 0, true", "false, 100, true"})
  @DisplayName("the restless dead sink back when their time is up or no one called them")
  void expires(final boolean bound, final int ticksLeft, final boolean expires) {
    assertEquals(expires, RestlessDeadEntity.expires(bound, ticksLeft));
  }
}
