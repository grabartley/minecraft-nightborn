package com.grahambartley.nightborn.oldblood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class OldBloodBlocksTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(FontLevel.class)
  @DisplayName("the font cannot be broken, pushed, crafted or carried away")
  void cannotBeTaken(final FontLevel level) {
    final var state = OldBloodBlocks.FONT.getDefaultState().with(OldBloodFontBlock.LEVEL, level);
    assertTrue(state.getHardness(null, null) < 0.0F);
    assertEquals(PistonBehavior.BLOCK, state.getPistonBehavior());
    assertEquals(Items.AIR, OldBloodBlocks.FONT.asItem());
    assertEquals(
        FontLevel.FULL, OldBloodBlocks.FONT.getDefaultState().get(OldBloodFontBlock.LEVEL));
  }
}
