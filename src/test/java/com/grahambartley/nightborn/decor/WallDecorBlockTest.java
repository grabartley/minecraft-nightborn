package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.gear.GearBlocks;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WallDecorBlockTest {

  static Stream<Arguments> hungPieces() {
    return Stream.of(
            DecorBlocks.WEAPON_RACK,
            DecorBlocks.WANTED_POSTER,
            DecorBlocks.TROPHY_MOUNT,
            GearBlocks.GOLD_WALL_CRUCIFIX,
            GearBlocks.SILVER_WALL_CRUCIFIX)
        .flatMap(
            block -> Direction.Type.HORIZONTAL.stream().map(facing -> Arguments.of(block, facing)));
  }

  @ParameterizedTest(name = "{0} facing {1}")
  @MethodSource("hungPieces")
  void hangsFlatAgainstTheBlockBehindIt(final Block block, final Direction facing) {
    final BlockState state = block.getDefaultState().with(WallDecorBlock.FACING, facing);
    final Vec3d wallSide = Vec3d.of(facing.getOpposite().getVector()).multiply(0.47);
    final Vec3d frontSide = Vec3d.of(facing.getVector()).multiply(0.47);
    final Vec3d centre = new Vec3d(0.5, 0.6, 0.5);
    assertTrue(inside(state, centre.add(wallSide)), "touches the wall");
    assertFalse(inside(state, centre.add(frontSide)), "leaves the front clear");
    assertEquals(facing.getOpposite(), WallDecorBlock.support(state));
  }

  private static boolean inside(final BlockState state, final Vec3d point) {
    return state
        .getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent())
        .getBoundingBoxes()
        .stream()
        .anyMatch(box -> box.expand(1.0e-6).contains(point));
  }
}
