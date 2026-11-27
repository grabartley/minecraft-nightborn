package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CampGapsTest {

  private static CampLayout.Tent tent(final int x, final int z, final BlockRotation rotation) {
    final List<CampLayout.Piece> pieces = new ArrayList<>();
    for (final TentPart part : TentPart.values()) {
      pieces.add(
          new CampLayout.Piece(
              part.offset(Direction.NORTH).add(0, 1, 1),
              TentBlocks.HUNTER_TENT
                  .getDefaultState()
                  .with(HunterTentBlock.FACING, Direction.NORTH)
                  .with(HunterTentBlock.PART, part)));
    }
    return new CampLayout.Tent(new BlockPos(x, 0, z), rotation, pieces);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "the shipped layout                | -3,2,NONE ; 3,2,NONE ; 0,-2,CLOCKWISE_180 |",
        "a tent beside the fire            | 0,1,NONE |a piece stands next to the campfire",
        "two tents touching                | -2,2,NONE ; 1,2,NONE |tents 0 and 1 leave no gap between them",
        "one gap between tents             | -2,2,NONE ; 2,2,NONE |"
      })
  @DisplayName("a camp leaves room to walk round the fire, between tents and into each tent")
  void findsCrampedCamps(final String name, final String tents, final String problem) {
    final List<CampLayout.Tent> list = new ArrayList<>();
    for (final String tent : tents.split(";")) {
      final String[] parts = tent.trim().split(",");
      list.add(
          tent(
              Integer.parseInt(parts[0]),
              Integer.parseInt(parts[1]),
              BlockRotation.valueOf(parts[2])));
    }
    assertEquals(
        Optional.ofNullable(problem), CampGaps.problem(new CampLayout(BlockPos.ORIGIN, list)));
  }

  @ParameterizedTest(name = "turned {0}, the mouth is at {1},{2}")
  @CsvSource({"NONE, 0, -1", "CLOCKWISE_180, 0, 1", "CLOCKWISE_90, 1, 0"})
  @DisplayName("a tent's open end is one step out from its bed foot")
  void mouthIsOneStepOutFromTheBedFoot(final BlockRotation rotation, final int x, final int z) {
    assertEquals(Optional.of(new BlockPos(x, 0, z)), CampGaps.mouth(tent(0, 0, rotation)));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "a crate at the open end, 0, false, tent 0 has its open end blocked",
    "a crate beside the open end, 1, false, ",
    "a tent with no roof, 0, true, tent 0 has no roof middle"
  })
  @DisplayName("a tent's own pieces never block its open end, and a tent needs a roof middle")
  void ownPiecesKeepTheMouthClear(
      final String name, final int crateX, final boolean roofless, final String problem) {
    final CampLayout.Tent whole = tent(5, 5, BlockRotation.NONE);
    final List<CampLayout.Piece> pieces = new ArrayList<>(whole.pieces());
    if (roofless) {
      pieces.removeIf(
          piece ->
              piece.state().isOf(TentBlocks.HUNTER_TENT)
                  && piece.state().get(HunterTentBlock.PART) == TentPart.ORIGIN);
    }
    pieces.add(new CampLayout.Piece(new BlockPos(crateX, 0, -1), Blocks.BARREL.getDefaultState()));
    final CampLayout.Tent tent = new CampLayout.Tent(whole.at(), whole.rotation(), pieces);
    assertEquals(
        Optional.ofNullable(problem),
        CampGaps.problem(new CampLayout(BlockPos.ORIGIN, List.of(tent))));
  }
}
