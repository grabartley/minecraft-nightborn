package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SceneTest {

  @ParameterizedTest(name = "made at {0}, now {1}, fresh for {2} -> {3}")
  @CsvSource({"100, 100, 24000, true", "100, 24100, 24000, true", "100, 24101, 24000, false"})
  @DisplayName("a scene can be read while its signs are fresh")
  void fresh(final long at, final long now, final int freshTicks, final boolean fresh) {
    assertEquals(
        fresh,
        new Scene(new UUID(0L, 1L), Optional.empty(), List.of(), Vec3d.ZERO, at, true)
            .fresh(now, freshTicks));
  }
}
