package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GarlicStringBlockTest {

  private static BlockState state(final Direction facing, final boolean hanging) {
    return GearBlocks.GARLIC_STRING
        .getDefaultState()
        .with(GarlicStringBlock.FACING, facing)
        .with(GarlicStringBlock.HANGING, hanging);
  }

  private static VoxelShape outline(final BlockState state) {
    return state.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent());
  }

  private static boolean contains(final VoxelShape shape, final Vec3d point) {
    return shape.getBoundingBoxes().stream().anyMatch(box -> box.contains(point));
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("a string on a wall hangs against the wall it is held by")
  void onAWall(final Direction facing) {
    final BlockState state = state(facing, false);
    final Direction wall = GarlicStringBlock.support(state);
    assertEquals(facing.getOpposite(), wall);
    final Vec3d centre = new Vec3d(0.5, 0.5, 0.5);
    final Vec3d nearWall = centre.add(Vec3d.of(wall.getVector()).multiply(0.45));
    final Vec3d awayFromWall = centre.add(Vec3d.of(facing.getVector()).multiply(0.45));
    assertTrue(contains(outline(state), nearWall));
    assertFalse(contains(outline(state), awayFromWall));
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("a string from the ceiling is held from above and hangs down the middle")
  void fromTheCeiling(final Direction facing) {
    final BlockState state = state(facing, true);
    assertEquals(Direction.UP, GarlicStringBlock.support(state));
    assertTrue(contains(outline(state), new Vec3d(0.5, 0.1, 0.5)));
    assertTrue(contains(outline(state), new Vec3d(0.5, 0.9, 0.5)));
    assertFalse(contains(outline(state), new Vec3d(0.05, 0.5, 0.05)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BlockRotation.class)
  @DisplayName("rotating a string turns it and keeps it hanging or on its wall")
  void rotates(final BlockRotation rotation) {
    for (final boolean hanging : new boolean[] {false, true}) {
      final BlockState turned = state(Direction.NORTH, hanging).rotate(rotation);
      assertEquals(rotation.rotate(Direction.NORTH), turned.get(GarlicStringBlock.FACING));
      assertEquals(hanging, turned.get(GarlicStringBlock.HANGING));
    }
  }

  @ParameterizedTest(name = "hanging {0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("a string never blocks movement, so a vampire walks into it")
  void noCollision(final boolean hanging) {
    assertTrue(
        state(Direction.NORTH, hanging)
            .getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)
            .isEmpty());
  }
}
