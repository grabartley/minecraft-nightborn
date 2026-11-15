package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BloodSplatterBlockTest {

  private static final Path TEXTURES =
      Path.of("src/main/resources/assets/nightborn/textures/block");

  @ParameterizedTest(name = "{0} facing {1}: ({2}, {3}, {4}) inside={5}")
  @CsvSource({
    "FLOOR, NORTH, 0.5, 0.03, 0.5, true",
    "FLOOR, NORTH, 0.5, 0.5, 0.5, false",
    "CEILING, EAST, 0.5, 0.97, 0.5, true",
    "CEILING, EAST, 0.5, 0.03, 0.5, false",
    "WALL, NORTH, 0.5, 0.5, 0.97, true",
    "WALL, NORTH, 0.5, 0.5, 0.03, false",
    "WALL, EAST, 0.03, 0.5, 0.5, true",
    "WALL, SOUTH, 0.5, 0.5, 0.03, true",
    "WALL, WEST, 0.97, 0.5, 0.5, true"
  })
  void liesFlatOnTheFaceItWasThrownAt(
      final BlockFace face,
      final Direction facing,
      final double x,
      final double y,
      final double z,
      final boolean inside) {
    final BlockState state =
        DecorBlocks.BLOOD_SPLATTER
            .getDefaultState()
            .with(WallMountedBlock.FACE, face)
            .with(WallMountedBlock.FACING, facing);
    assertEquals(
        inside,
        state
            .getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent())
            .getBoundingBoxes()
            .stream()
            .anyMatch(box -> box.contains(new Vec3d(x, y, z))));
    assertTrue(
        state
            .getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent())
            .isEmpty());
  }

  @ParameterizedTest(name = "splat {0}")
  @ValueSource(ints = {0, 1, 2, 3})
  void everySplatHasItsOwnShape(final int splat) {
    assertTrue(BloodSplatterBlock.SPLAT.getValues().contains(splat));
    assertTrue(Files.exists(TEXTURES.resolve("blood_splatter_" + splat + ".png")));
  }
}
