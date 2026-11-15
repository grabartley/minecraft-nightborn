package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ChairBlockTest {

  private static final Vec3d BACKREST = new Vec3d(0.5, 0.75, 0.8);
  private static final Vec3d CREST = new Vec3d(0.5, 1.35, 0.8);
  private static final Vec3d ABOVE_SEAT = new Vec3d(0.5, 0.75, 0.3);

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  void putsTheTallBackrestBehindTheSitter(final Direction facing) {
    final BlockState state =
        DecorBlocks.ORNATE_CHAIR.getDefaultState().with(ChairBlock.FACING, facing);
    final VoxelShape shape =
        state.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent());
    assertTrue(contains(shape, FacingShapes.turn(BACKREST, facing)));
    assertTrue(contains(shape, FacingShapes.turn(CREST, facing)));
    assertFalse(contains(shape, FacingShapes.turn(ABOVE_SEAT, facing)));
  }

  private static boolean contains(final VoxelShape shape, final Vec3d point) {
    return shape.getBoundingBoxes().stream().anyMatch(box -> box.contains(point));
  }
}
