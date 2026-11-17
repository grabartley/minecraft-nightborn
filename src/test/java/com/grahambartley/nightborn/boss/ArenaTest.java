package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArenaTest {

  private static final Arena ARENA = new Arena(new BlockPos(10, 64, -20), 16);

  @ParameterizedTest(name = "({0}, {1}, {2}) inside: {3}")
  @CsvSource({
    "10.5, 64.0, -19.5, true",
    "26.5, 64.0, -19.5, true",
    "26.6, 64.0, -19.5, false",
    "10.5, 64.0, -35.5, true",
    "10.5, 64.0, -35.6, false",
    "22.0, 64.0, -8.0, false",
    "21.0, 64.0, -9.0, true",
    "10.5, 80.0, -19.5, true",
    "10.5, 80.1, -19.5, false",
    "10.5, 58.0, -19.5, true",
    "10.5, 57.9, -19.5, false"
  })
  @DisplayName("the arena is a circle round its centre, with a floor and a ceiling")
  void holds(final double x, final double y, final double z, final boolean inside) {
    assertEquals(inside, ARENA.holds(x, y, z));
  }

  @ParameterizedTest(name = "({0}, {1}, {2}) is pulled to ({3}, {4}, {5})")
  @CsvSource({
    "12.5, 66.0, -19.5, 12.5, 66.0, -19.5",
    "40.5, 64.0, -19.5, 24.5, 64.0, -19.5",
    "10.5, 64.0, -59.5, 10.5, 64.0, -33.5",
    "10.5, 120.0, -19.5, 10.5, 80.0, -19.5",
    "10.5, 0.0, -19.5, 10.5, 58.0, -19.5"
  })
  @DisplayName("a point outside is pulled back to two blocks inside the wall")
  void pullsIn(
      final double x,
      final double y,
      final double z,
      final double wantX,
      final double wantY,
      final double wantZ) {
    final Vec3d pulled = ARENA.pullIn(new Vec3d(x, y, z), 2.0);
    assertEquals(wantX, pulled.x, 1.0E-9);
    assertEquals(wantY, pulled.y, 1.0E-9);
    assertEquals(wantZ, pulled.z, 1.0E-9);
    assertTrue(ARENA.holds(pulled.x, pulled.y, pulled.z));
  }

  @ParameterizedTest(name = "{0} blocks from the floor of a room: inside {1}")
  @CsvSource({
    "0, true",
    "-1, true",
    "-1.1, false",
    "-6, false",
    "8, true",
    "8.1, false",
    "16, false"
  })
  @DisplayName(
      "a room's arena stops one block under its floor and eight above, so it never reaches the rooms below")
  void room(final double up, final boolean inside) {
    final Arena room = Arena.room(new BlockPos(10, 64, -20), 10);
    assertEquals(inside, room.holds(10.5, 64.0 + up, -19.5));
    assertEquals(new Arena(new BlockPos(10, 64, -20), 10, 1, 8), room);
  }

  @ParameterizedTest(name = "radius {0} becomes {1}")
  @CsvSource({"-3, 4", "0, 4", "4, 4", "5, 5", "48, 48"})
  @DisplayName("an arena is never smaller than four blocks")
  void smallest(final int given, final int kept) {
    assertEquals(kept, new Arena(BlockPos.ORIGIN, given).radius());
  }

  @ParameterizedTest(name = "centre ({0}, {1}, {2}) radius {3}, {4} below and {5} above")
  @CsvSource({"0, 0, 0, 4, 6, 16", "10, 64, -20, 16, 1, 8", "-29000000, 319, 29000000, 48, 0, 1"})
  @DisplayName("an arena survives a save and a trip across the network")
  void roundTrips(
      final int x, final int y, final int z, final int radius, final int below, final int above) {
    final Arena arena = new Arena(new BlockPos(x, y, z), radius, below, above);
    final NbtCompound nbt = new NbtCompound();
    arena.write(nbt);
    assertEquals(Optional.of(arena), Arena.read(nbt));
    final ByteBuf buf = Unpooled.buffer();
    Arena.PACKET_CODEC.encode(buf, arena);
    assertEquals(arena, Arena.PACKET_CODEC.decode(buf));
  }

  @Test
  @DisplayName("a save with no arena reads as none")
  void readsNothing() {
    assertEquals(Optional.empty(), Arena.read(new NbtCompound()));
  }
}
