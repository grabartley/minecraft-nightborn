package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SceneKillsTest {

  private static Scene kill(final long id, final double x, final long at) {
    return new Scene(new UUID(0L, id), Optional.empty(), List.of(), new Vec3d(x, 0, 0), at);
  }

  @ParameterizedTest(name = "looking at x {0}, now {1} -> vampire {2}")
  @CsvSource({"0, 0, 1", "3, 0, 2", "10, 0, 0", "0, 30000, 0", "2.4, 0, 2"})
  @DisplayName("the nearest fresh kill within the radius is read, and none further or older")
  void nearest(final double x, final long now, final long expected) {
    final SceneKills kills = new SceneKills(8);
    kills.add(kill(1L, 0.0, 0L));
    kills.add(kill(2L, 4.0, 0L));
    assertEquals(
        expected,
        kills
            .nearest(new Vec3d(x, 0, 0), 4.0, now, 24000)
            .map(scene -> scene.vampire().getLeastSignificantBits())
            .orElse(0L));
  }

  @ParameterizedTest(name = "{0} kills kept at most {1} -> {2}")
  @CsvSource({"3, 8, 3", "8, 8, 8", "12, 8, 8"})
  @DisplayName("only the newest kills are kept")
  void keepsTheNewest(final int added, final int most, final int kept) {
    final SceneKills kills = new SceneKills(most);
    for (int i = 0; i < added; i++) {
      kills.add(kill(i + 1L, i, 0L));
    }
    assertEquals(kept, kills.size());
    assertEquals(
        added == 0 ? 0L : added,
        kills
            .nearest(new Vec3d(added - 1.0, 0, 0), 0.5, 0L, 24000)
            .map(scene -> scene.vampire().getLeastSignificantBits())
            .orElse(0L));
  }
}
