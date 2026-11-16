package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.MesmerisePower;
import java.util.UUID;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GazeTest {

  @ParameterizedTest(name = "holding {0} at tick {1} -> {2}")
  @CsvSource({
    "true, 90, HOLD",
    "true, 99, HOLD",
    "true, 100, LAND",
    "true, 140, LAND",
    "false, 90, BREAK",
    "false, 100, BREAK"
  })
  @DisplayName("a gaze lands once held to its tick and breaks the moment it slips")
  void step(final boolean holding, final long now, final Gaze.Step expected) {
    final Gaze gaze =
        new Gaze(World.OVERWORLD, UUID.randomUUID(), 100L, new MesmerisePower(12, 20, 200, 60, 2));
    assertEquals(expected, gaze.step(holding, now));
  }
}
