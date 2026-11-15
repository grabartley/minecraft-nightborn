package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FurnitureBlockEntityTest {

  @ParameterizedTest(name = "{0}, toggled {1}")
  @CsvSource({
    "candelabra, false",
    "candelabra, true",
    "chandelier, true",
    "burgundy_velvet_drape, false",
    "emerald_velvet_drape, true"
  })
  @DisplayName("a piece of furniture is active exactly when its block's toggle property is set")
  void followsItsBlocksToggle(final String id, final boolean toggled) {
    final FurnitureBlock block = (FurnitureBlock) DecorBlocks.get(id);
    final AnimatedFurniture furniture = (AnimatedFurniture) block;
    final BlockState state = block.getDefaultState().with(furniture.toggleProperty(), toggled);
    final FurnitureBlockEntity entity = new FurnitureBlockEntity(BlockPos.ORIGIN, state);
    assertSame(furniture, entity.furniture());
    assertEquals(toggled, entity.isActive());
  }
}
