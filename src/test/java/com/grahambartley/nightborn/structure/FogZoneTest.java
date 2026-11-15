package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.BlockBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FogZoneTest {

  private static final FogZone ZONE = new FogZone(0.0, 64.0, 0.0, 40.0);

  @Test
  @DisplayName("the heart sits at the middle of the first piece, and reach covers the whole start")
  void centresOnTheHeart() {
    final FogZone zone =
        FogZone.around(new BlockBox(0, 60, 0, 9, 69, 9), new BlockBox(-20, 60, 0, 9, 69, 9));
    assertEquals(5.0, zone.x());
    assertEquals(65.0, zone.y());
    assertEquals(5.0, zone.z());
    assertEquals(Math.sqrt(25 * 25 + 5 * 5 + 5 * 5) + FogZone.MARGIN, zone.reach(), 1.0E-9);
  }

  @ParameterizedTest(name = "at {0},{1},{2} closeness {3}")
  @CsvSource({
    "0, 64, 0, 1.0",
    "20, 64, 0, 0.5",
    "0, 64, 30, 0.25",
    "40, 64, 0, 0.0",
    "100, 64, 0, 0.0",
    "0, 84, 0, 0.5"
  })
  @DisplayName("closeness runs from one at the heart to zero at the edge of its reach")
  void closeness(final double x, final double y, final double z, final float expected) {
    assertEquals(expected, ZONE.closeness(x, y, z), 1.0E-6);
  }

  @ParameterizedTest(name = "at {0},{1},{2} inside: {3}")
  @CsvSource({"0, 64, 0, true", "39.9, 64, 0, true", "40, 64, 0, false", "0, 200, 0, false"})
  @DisplayName("a point is inside a zone only within its reach")
  void contains(final double x, final double y, final double z, final boolean inside) {
    assertEquals(inside, ZONE.contains(x, y, z));
  }

  @Test
  @DisplayName("a zone crosses the network unchanged")
  void roundTrips() {
    final ByteBuf buf = Unpooled.buffer();
    FogZone.PACKET_CODEC.encode(buf, new FogZone(1.5, -12.0, 300.25, 77.0));
    assertEquals(new FogZone(1.5, -12.0, 300.25, 77.0), FogZone.PACKET_CODEC.decode(buf));
  }
}
