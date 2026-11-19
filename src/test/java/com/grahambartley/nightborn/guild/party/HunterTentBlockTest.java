package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterTentBlockTest {

  private static BlockState state(final TentPart part) {
    return TentBlocks.HUNTER_TENT.getDefaultState().with(HunterTentBlock.PART, part);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(TentPart.class)
  @DisplayName("only the roof's middle draws the tent, so it is drawn once")
  void onlyTheOriginDraws(final TentPart part) {
    final HunterTentBlock block = (HunterTentBlock) TentBlocks.HUNTER_TENT;
    if (part == TentPart.ORIGIN) {
      assertEquals(BlockRenderType.ENTITYBLOCK_ANIMATED, state(part).getRenderType());
      assertInstanceOf(
          HunterTentBlockEntity.class, block.createBlockEntity(BlockPos.ORIGIN, state(part)));
    } else {
      assertEquals(BlockRenderType.INVISIBLE, state(part).getRenderType());
      assertNull(block.createBlockEntity(BlockPos.ORIGIN, state(part)));
    }
  }

  @ParameterizedTest(name = "{0} turned {1} faces {2}")
  @CsvSource({
    "NORTH, NONE, NORTH",
    "NORTH, CLOCKWISE_180, SOUTH",
    "NORTH, CLOCKWISE_90, EAST",
    "EAST, COUNTERCLOCKWISE_90, NORTH"
  })
  @DisplayName("a tent in a turned camp layout turns its open front with it")
  void turnsWithTheLayout(
      final Direction facing, final BlockRotation rotation, final Direction turned) {
    final BlockState state = state(TentPart.LEFT_BACK).with(HunterTentBlock.FACING, facing);
    assertEquals(turned, state.rotate(rotation).get(HunterTentBlock.FACING));
    assertEquals(TentPart.LEFT_BACK, state.rotate(rotation).get(HunterTentBlock.PART));
  }

  @ParameterizedTest(name = "facing {0}, mirrored {1}")
  @CsvSource({"NORTH, FRONT_BACK", "NORTH, LEFT_RIGHT", "EAST, FRONT_BACK", "WEST, LEFT_RIGHT"})
  @DisplayName("a mirrored tent keeps every cell pointing at its roof's middle")
  void mirroredCellsStillFindTheOrigin(final Direction facing, final BlockMirror mirror) {
    for (final TentPart part : TentPart.values()) {
      final BlockPos offset = part.offset(facing);
      final BlockPos flipped =
          mirror == BlockMirror.FRONT_BACK
              ? new BlockPos(-offset.getX(), offset.getY(), offset.getZ())
              : new BlockPos(offset.getX(), offset.getY(), -offset.getZ());
      final BlockState mirrored = state(part).with(HunterTentBlock.FACING, facing).mirror(mirror);
      assertEquals(
          BlockPos.ORIGIN,
          mirrored.get(HunterTentBlock.PART).origin(flipped, mirrored.get(HunterTentBlock.FACING)),
          part.asString());
    }
  }
}
