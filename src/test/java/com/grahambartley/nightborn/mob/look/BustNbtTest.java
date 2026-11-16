package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BustNbtTest {

  private static final float TOLERANCE = 1.0E-6F;

  @ParameterizedTest(name = "{0} saved {1} loads {2}")
  @CsvSource({
    "FEMALE, 0.7, 0.7",
    "FEMALE, 1.0, 1.0",
    "FEMALE, 1.3, 1.3",
    "FEMALE, 0.2, 0.7",
    "FEMALE, 4.0, 1.3",
    "MALE, 1.3, 1.0",
    "MALE, 0.7, 1.0"
  })
  @DisplayName("a saved bust loads back within its rig's range")
  void loadsWithinTheRigRange(final VampireRig rig, final float saved, final float loaded) {
    final NbtCompound nbt = new NbtCompound();
    BustNbt.write(nbt, saved);
    assertEquals(loaded, BustNbt.read(nbt, rig), TOLERANCE);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(VampireRig.class)
  @DisplayName("a body saved before busts existed loads flat")
  void oldSavesLoadFlat(final VampireRig rig) {
    assertEquals(Figure.FLAT, BustNbt.read(new NbtCompound(), rig), TOLERANCE);
  }
}
