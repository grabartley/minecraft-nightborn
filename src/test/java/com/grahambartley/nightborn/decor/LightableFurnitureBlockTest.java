package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LightableFurnitureBlockTest {

  @ParameterizedTest(name = "{0} lit={1} gives light {2}")
  @CsvSource({
    "candelabra, false, 0",
    "candelabra, true, 12",
    "chandelier, false, 0",
    "chandelier, true, 15"
  })
  void lightsAndGlowsOnlyWhileLit(final String id, final boolean lit, final int light) {
    final LightableFurnitureBlock block = (LightableFurnitureBlock) DecorBlocks.get(id);
    final BlockState state = block.getDefaultState().with(LightableFurnitureBlock.LIT, lit);
    assertEquals(light, state.getLuminance());
    assertEquals(lit, block.glows(state));
    assertEquals(LightableFurnitureBlock.LIT, block.toggleProperty());
  }

  @ParameterizedTest(name = "{0} rests on its {1} side")
  @CsvSource({"candelabra, DOWN", "chandelier, UP"})
  void restsOnTheSideItNeeds(final String id, final Direction side) {
    assertEquals(side, ((LightableFurnitureBlock) DecorBlocks.get(id)).supportSide());
  }

  @ParameterizedTest(name = "{0} starts out")
  @CsvSource({"candelabra", "chandelier"})
  void startsUnlitFacingNorth(final String id) {
    final BlockState state = DecorBlocks.get(id).getDefaultState();
    assertEquals(false, state.get(LightableFurnitureBlock.LIT));
    assertEquals(Direction.NORTH, state.get(FurnitureBlock.FACING));
  }
}
