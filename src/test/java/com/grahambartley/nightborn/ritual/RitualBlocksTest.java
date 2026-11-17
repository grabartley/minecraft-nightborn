package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.registry.Registries;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RitualBlocksTest {

  @Test
  @DisplayName("the altar, its item and its block entity share one id")
  void registered() {
    assertEquals("nightborn:ritual_altar", Registries.BLOCK.getId(RitualBlocks.ALTAR).toString());
    assertEquals(
        "nightborn:ritual_altar", Registries.ITEM.getId(RitualBlocks.ALTAR_ITEM).toString());
    assertEquals(
        "nightborn:ritual_altar",
        Registries.BLOCK_ENTITY_TYPE.getId(RitualBlocks.ALTAR_ENTITY).toString());
    assertTrue(RitualBlocks.ALTAR_ENTITY.supports(RitualBlocks.ALTAR.getDefaultState()));
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("the skull of the Eldest is a rare block that can face any way")
  void skull(final Direction facing) {
    assertEquals(
        "nightborn:eldest_skull", Registries.BLOCK.getId(RitualBlocks.ELDEST_SKULL).toString());
    assertEquals(
        "nightborn:eldest_skull", Registries.ITEM.getId(RitualBlocks.ELDEST_SKULL_ITEM).toString());
    assertEquals(Rarity.RARE, RitualBlocks.ELDEST_SKULL_ITEM.getDefaultStack().getRarity());
    assertEquals(
        facing,
        RitualBlocks.ELDEST_SKULL
            .getDefaultState()
            .with(HorizontalFacingBlock.FACING, facing)
            .get(HorizontalFacingBlock.FACING));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(AltarPhase.class)
  @DisplayName("the altar glows only while the ritual runs")
  void glows(final AltarPhase phase) {
    final BlockState state =
        RitualBlocks.ALTAR.getDefaultState().with(RitualAltarBlock.PHASE, phase);
    assertEquals(phase.light(), state.getLuminance());
  }
}
