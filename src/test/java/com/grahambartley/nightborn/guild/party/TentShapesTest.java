package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TentShapesTest {

  private static double heightAt(final VoxelShape shape, final double x, final double z) {
    return shape.getBoundingBoxes().stream()
        .filter(box -> box.minX <= x && x <= box.maxX && box.minZ <= z && z <= box.maxZ)
        .mapToDouble(box -> box.maxY)
        .max()
        .orElse(0.0);
  }

  @ParameterizedTest(name = "facing {0}, {1}")
  @CsvSource({
    "NORTH, RIGHT_MIDDLE",
    "NORTH, LEFT_FRONT",
    "SOUTH, RIGHT_BACK",
    "SOUTH, LEFT_MIDDLE",
    "EAST, RIGHT_FRONT",
    "WEST, LEFT_BACK"
  })
  @DisplayName("a side wall stands tall by the ridge and slopes to the ground outside")
  void sidesSlopeAwayFromTheRidge(final Direction facing, final TentPart part) {
    final Direction out =
        part.right() > 0 ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();
    final VoxelShape shape = TentShapes.collision(part, facing);
    final double inner =
        heightAt(shape, 0.5 - 0.4 * out.getOffsetX(), 0.5 - 0.4 * out.getOffsetZ());
    final double outer =
        heightAt(shape, 0.5 + 0.4 * out.getOffsetX(), 0.5 + 0.4 * out.getOffsetZ());
    assertEquals(1.0, inner);
    assertTrue(outer <= 5.0 / 16.0, "outer edge is low, was " + outer);
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the back wall closes the back of its cell and leaves the front open")
  void backWallSitsAtTheBack(final Direction facing) {
    final Direction back = facing.getOpposite();
    final VoxelShape shape = TentShapes.collision(TentPart.BACK, facing);
    assertEquals(
        1.0, heightAt(shape, 0.5 + 0.45 * back.getOffsetX(), 0.5 + 0.45 * back.getOffsetZ()));
    assertEquals(
        0.0, heightAt(shape, 0.5 - 0.45 * back.getOffsetX(), 0.5 - 0.45 * back.getOffsetZ()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = TentPart.class,
      names = {"ROOF_FRONT", "ROOF_MIDDLE", "ROOF_BACK"})
  @DisplayName("the roof can be aimed at but never blocks or smothers anyone inside")
  void roofHasNoCollision(final TentPart part) {
    for (final Direction facing : Direction.Type.HORIZONTAL) {
      assertTrue(TentShapes.collision(part, facing).isEmpty());
      assertFalse(TentShapes.outline(part, facing).isEmpty());
    }
  }
}
