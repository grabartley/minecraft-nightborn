package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TableBlockTest {

  @ParameterizedTest(name = "({0}, {1}, {2}) solid={3}")
  @CsvSource({
    "0.05, 0.9, 0.05, true",
    "0.95, 0.9, 0.95, true",
    "0.5, 0.4, 0.5, true",
    "0.05, 0.4, 0.05, false",
    "0.95, 0.1, 0.5, false"
  })
  void hasAFullTopOnACentralPedestal(
      final double x, final double y, final double z, final boolean solid) {
    final boolean inside =
        DecorBlocks.ORNATE_TABLE
            .getDefaultState()
            .getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent())
            .getBoundingBoxes()
            .stream()
            .anyMatch(box -> box.contains(new Vec3d(x, y, z)));
    assertEquals(solid, inside);
  }
}
