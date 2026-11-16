package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StatueBlockTest {

  @ParameterizedTest(name = "a gargoyle facing {0} sits on its base")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  void sitsOnItsBaseWhicheverWayItFaces(final Direction facing) {
    final BlockState state =
        DecorBlocks.GARGOYLE.getDefaultState().with(StatueBlock.FACING, facing);
    assertTrue(inside(state, new Vec3d(0.5, 0.5, 0.5)), "body");
    assertTrue(inside(state, new Vec3d(0.15, 0.05, 0.15)), "base corner");
    assertFalse(inside(state, new Vec3d(0.15, 0.5, 0.15)), "open beside the body");
    assertEquals(facing, state.get(StatueBlock.FACING));
  }

  @ParameterizedTest(name = "the default faces {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH"})
  void facesNorthByDefault(final Direction facing) {
    assertEquals(facing, DecorBlocks.GARGOYLE.getDefaultState().get(StatueBlock.FACING));
  }

  private static boolean inside(final BlockState state, final Vec3d point) {
    return state
        .getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent())
        .getBoundingBoxes()
        .stream()
        .anyMatch(box -> box.expand(1.0e-6).contains(point));
  }
}
