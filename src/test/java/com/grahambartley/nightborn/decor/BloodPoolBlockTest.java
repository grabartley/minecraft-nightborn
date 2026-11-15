package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BloodPoolBlockTest {

  private static final BlockState POOL = DecorBlocks.BLOOD_POOL.getDefaultState();

  @ParameterizedTest(name = "({0}, {1}, {2}) inside={3}")
  @CsvSource({
    "0.05, 0.03, 0.05, true",
    "0.95, 0.03, 0.5, true",
    "0.5, 0.2, 0.5, false",
    "0.5, 0.9, 0.5, false"
  })
  void coversTheFloorAndNothingAbove(
      final double x, final double y, final double z, final boolean inside) {
    assertEquals(
        inside,
        POOL
            .getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent())
            .getBoundingBoxes()
            .stream()
            .anyMatch(box -> box.contains(new Vec3d(x, y, z))));
  }

  @Test
  void isWalkedThrough() {
    assertTrue(
        POOL.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent())
            .isEmpty());
  }
}
