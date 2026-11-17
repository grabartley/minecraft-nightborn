package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SpotNbtTest {

  @ParameterizedTest(name = "({0}, {1}, {2})")
  @CsvSource({"0, 0, 0", "10.5, 64.25, -20.75", "-29999999.5, 319, 29999999.5"})
  @DisplayName("a spot survives a save, and a save without one reads as none")
  void savesASpot(final double x, final double y, final double z) {
    final NbtCompound nbt = new NbtCompound();
    nbt.put("Spot", SpotNbt.list(new Vec3d(x, y, z)));
    assertEquals(Optional.of(new Vec3d(x, y, z)), SpotNbt.vec(nbt, "Spot"));
    assertEquals(Optional.empty(), SpotNbt.vec(nbt, "Other"));
  }
}
