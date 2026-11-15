package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CandelabraBlockTest {

  private static final double FLAME_REACH = 1.0 / 16;

  private static CandelabraBlock block() {
    return (CandelabraBlock) DecorBlocks.get("candelabra");
  }

  @ParameterizedTest(name = "lit={0} gives light {1}")
  @CsvSource({"false, 0", "true, 12"})
  void lightsAndGlowsOnlyWhileLit(final boolean lit, final int light) {
    final BlockState state = block().getDefaultState().with(LightableFurnitureBlock.LIT, lit);
    assertEquals(light, state.getLuminance());
    assertEquals(lit, block().glows(state));
    assertEquals(LightableFurnitureBlock.LIT, block().toggleProperty());
    assertEquals("candelabra", block().artModel());
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  void everyWickSitsOnTheModelInEveryFacing(final Direction facing) {
    final BlockState state = block().getDefaultState().with(LightableFurnitureBlock.FACING, facing);
    final Box reach = block().shapeFor(state).getBoundingBox().expand(FLAME_REACH);
    for (final Vec3d wick : block().wicks()) {
      final Vec3d turned = FacingShapes.turn(wick, facing);
      assertTrue(reach.contains(turned), "wick " + turned + " is off the model " + reach);
    }
  }
}
