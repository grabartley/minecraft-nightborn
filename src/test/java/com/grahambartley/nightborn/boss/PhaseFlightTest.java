package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseFlightTest {

  private static final PhaseFlight FLIGHT = new PhaseFlight(6.0F, 9.0F, 0.3F, 80);

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"height\":6,\"radius\":9} | true",
        "{\"height\":6,\"radius\":9,\"speed\":0.3,\"ground_ticks\":80} | true",
        "{\"radius\":9} | false",
        "{\"height\":6} | false",
        "{\"height\":0,\"radius\":9} | false",
        "{\"height\":6,\"radius\":9,\"speed\":0} | false"
      })
  @DisplayName("a flight needs a height and a ring to circle")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, PhaseFlight.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "fighting {0}, held {1}, diving {2}, {3} ticks since landing: {4}")
  @CsvSource({
    "true, false, false, 80, true",
    "true, false, false, 5000, true",
    "true, false, false, 79, false",
    "false, false, false, 5000, false",
    "true, true, false, 5000, false",
    "true, false, true, 5000, false"
  })
  @DisplayName(
      "the boss flies while it fights, but not while it dives, is held, or has just landed")
  void airborne(
      final boolean fighting,
      final boolean held,
      final boolean diving,
      final long sinceLanding,
      final boolean airborne) {
    assertEquals(airborne, FLIGHT.airborne(fighting, held, diving, sinceLanding));
  }

  @ParameterizedTest(name = "an arena of radius {0} reaching {1} up: ring {2}, {3} up")
  @CsvSource({"16, 16, 9.0, 6.0", "8, 16, 6.0, 6.0", "16, 5, 9.0, 3.0", "4, 2, 2.0, 1.0"})
  @DisplayName("the boss circles inside the arena, under its ceiling, however small it is")
  void spot(final int radius, final int above, final double ring, final double up) {
    final Arena arena = new Arena(new BlockPos(10, 64, -20), radius, 1, above);
    for (final long time : new long[] {0L, 37L, 1000L}) {
      final Vec3d spot = FLIGHT.spot(arena, time);
      assertEquals(ring, Math.hypot(spot.x - arena.midX(), spot.z - arena.midZ()), 1.0E-9);
      assertEquals(64.0 + up, spot.y, 1.0E-9);
      assertTrue(arena.holds(spot.x, spot.y, spot.z));
    }
  }

  @ParameterizedTest(name = "{0} blocks from its spot: moves {1}")
  @CsvSource({"0.0, 0.0", "0.4, 0.4", "0.6, 0.6", "5.0, 0.6", "40.0, 0.6"})
  @DisplayName("the boss closes on its spot at no more than twice its circling speed")
  void steers(final double gap, final double moved) {
    final Vec3d step = FLIGHT.steer(Vec3d.ZERO, new Vec3d(gap, 0.0, 0.0));
    assertEquals(moved, step.length(), 1.0E-6);
  }
}
