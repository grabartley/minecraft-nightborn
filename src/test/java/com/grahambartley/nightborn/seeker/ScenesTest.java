package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScenesTest {

  private static Optional<Scene> scene(final boolean present, final long id) {
    return present
        ? Optional.of(
            new Scene(new UUID(0L, id), Optional.empty(), List.of(), Vec3d.ZERO, 0L, true))
        : Optional.empty();
  }

  @ParameterizedTest(name = "victim {0}, coffin {1}, kill {2} -> {3}")
  @CsvSource({
    "true, true, true, 1",
    "false, true, true, 2",
    "false, false, true, 3",
    "false, false, false, 0",
    "true, false, false, 1"
  })
  @DisplayName("a bitten victim is read first, then a coffin, then the nearest kill")
  void pick(final boolean victim, final boolean coffin, final boolean kill, final long read) {
    assertEquals(
        read,
        Scenes.pick(scene(victim, 1L), () -> scene(coffin, 2L), () -> scene(kill, 3L))
            .map(found -> found.vampire().getLeastSignificantBits())
            .orElse(0L));
  }

  @ParameterizedTest(name = "block hit {0} -> x {1}")
  @CsvSource({"true, 5", "false, 1"})
  @DisplayName("a kill is sought where the hunter looks, or around them when they look at nothing")
  void spot(final boolean hit, final double x) {
    assertEquals(x, Scenes.spot(hit, new Vec3d(5, 0, 0), new Vec3d(1, 0, 0)).x);
  }
}
