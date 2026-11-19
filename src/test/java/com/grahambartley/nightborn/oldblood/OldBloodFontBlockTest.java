package com.grahambartley.nightborn.oldblood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class OldBloodFontBlockTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(FontLevel.class)
  @DisplayName("the font is drawn by its animated model at every fill level")
  void drawsItsModel(final FontLevel level) {
    final var state = OldBloodBlocks.FONT.getDefaultState().with(OldBloodFontBlock.LEVEL, level);
    assertEquals(BlockRenderType.ENTITYBLOCK_ANIMATED, state.getRenderType());
    assertInstanceOf(
        OldBloodFontBlockEntity.class,
        ((OldBloodFontBlock) OldBloodBlocks.FONT).createBlockEntity(BlockPos.ORIGIN, state));
  }
}
