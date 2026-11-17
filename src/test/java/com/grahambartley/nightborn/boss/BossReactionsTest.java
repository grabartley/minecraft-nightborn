package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossReactionsTest {

  @ParameterizedTest(name = "foe at ({0}, {1}), {2} blocks: lands at ({3}, {4})")
  @CsvSource({
    "-3, 0, 8, 8, 0",
    "0, 3, 8, 0, -8",
    "0, 0, 8, 8, 0",
    "-3, 0, 40, 14, 0",
    "3, 0, 40, -14, 0"
  })
  @DisplayName("the boss breaks away straight from its attacker, and never through the arena wall")
  void away(
      final double foeX, final double foeZ, final double blocks, final double x, final double z) {
    final Arena arena = new Arena(new BlockPos(0, 64, 0), 16);
    final Vec3d self = new Vec3d(0.5, 64.0, 0.5);
    final Vec3d to =
        BossReactions.away(self, new Vec3d(0.5 + foeX, 64.0, 0.5 + foeZ), blocks, arena);
    assertEquals(0.5 + x, to.x, 1.0E-9);
    assertEquals(0.5 + z, to.z, 1.0E-9);
    assertEquals(64.0, to.y, 1.0E-9);
  }

  @ParameterizedTest(name = "({0}, {1}, {2})")
  @CsvSource({"0, 0, 0", "10.5, 64.25, -20.75", "-29999999.5, 319, 29999999.5"})
  @DisplayName("a spot survives a save, and a save without one reads as none")
  void savesASpot(final double x, final double y, final double z) {
    final NbtCompound nbt = new NbtCompound();
    nbt.put("Spot", BossReactions.list(new Vec3d(x, y, z)));
    assertEquals(Optional.of(new Vec3d(x, y, z)), BossReactions.vec(nbt, "Spot"));
    assertEquals(Optional.empty(), BossReactions.vec(nbt, "Other"));
  }
}
