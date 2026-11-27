package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TentPathsTest {

  @ParameterizedTest(name = "{0} standing reads as {1}")
  @CsvSource({
    "LEFT_FRONT, FENCE",
    "LEFT_MIDDLE, FENCE",
    "LEFT_BACK, FENCE",
    "RIGHT_FRONT, FENCE",
    "RIGHT_MIDDLE, FENCE",
    "RIGHT_BACK, FENCE",
    "BACK, FENCE",
    "ROOF_BACK, FENCE",
    "ROOF_FRONT, ",
    "ROOF_MIDDLE, "
  })
  @DisplayName("a standing tent is solid to paths everywhere but over its open end")
  void standingTentIsSolidButForItsMouth(final TentPart part, final PathNodeType type) {
    assertEquals(type, TentPaths.type(part, false, false));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(TentPart.class)
  @DisplayName("a struck tent lying flat leaves paths to the game")
  void struckTentLeavesPathsAlone(final TentPart part) {
    assertNull(TentPaths.type(part, true, false));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(TentPart.class)
  @DisplayName("a tent next to a path cell never changes that cell")
  void neighbourCellsAreLeftAlone(final TentPart part) {
    assertNull(TentPaths.type(part, false, true));
  }

  @ParameterizedTest(name = "{0}, struck {1}")
  @CsvSource({
    "LEFT_MIDDLE, false, FENCE",
    "ROOF_BACK, false, FENCE",
    "ROOF_FRONT, false, ",
    "LEFT_MIDDLE, true, "
  })
  @DisplayName("once registered, the game's path type lookup reads the tent the same way")
  void registeredTypeReachesTheLookup(
      final TentPart part, final boolean struck, final PathNodeType type) {
    TentPaths.register();
    final BlockState state =
        TentBlocks.HUNTER_TENT
            .getDefaultState()
            .with(HunterTentBlock.PART, part)
            .with(HunterTentBlock.STRUCK, struck);
    assertEquals(
        type,
        LandPathNodeTypesRegistry.getPathNodeType(
            state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN, false));
  }
}
