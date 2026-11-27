package com.grahambartley.nightborn.guild.party;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class CampGaps {

  private CampGaps() {}

  public static Optional<String> problem(final CampLayout layout) {
    final List<BlockPos> pieces = layout.pieces().stream().map(CampLayout.Piece::pos).toList();
    if (pieces.stream().anyMatch(pos -> apart(pos, layout.campfire()) <= 1)) {
      return Optional.of("a piece stands next to the campfire");
    }
    final List<CampLayout.Tent> tents = layout.tents();
    for (int i = 0; i < tents.size(); i++) {
      for (int j = i + 1; j < tents.size(); j++) {
        if (apart(tents.get(i), tents.get(j)) < 2) {
          return Optional.of("tents " + i + " and " + j + " leave no gap between them");
        }
      }
      final Optional<BlockPos> mouth = mouth(tents.get(i));
      if (mouth.isEmpty()) {
        return Optional.of("tent " + i + " has no origin piece");
      }
      if (pieces.stream().anyMatch(pos -> apart(pos, mouth.get()) == 0)) {
        return Optional.of("tent " + i + " has its open end blocked");
      }
    }
    return Optional.empty();
  }

  static Optional<BlockPos> mouth(final CampLayout.Tent tent) {
    return tent.placed().stream()
        .filter(piece -> piece.state().isOf(TentBlocks.HUNTER_TENT))
        .filter(piece -> piece.state().get(HunterTentBlock.PART) == TentPart.ORIGIN)
        .findFirst()
        .map(
            origin -> {
              final BlockState state = origin.state();
              final Direction facing = state.get(HunterTentBlock.FACING);
              return origin.pos().add(TentPart.ROOF_FRONT.offset(facing)).down().offset(facing);
            });
  }

  private static int apart(final CampLayout.Tent one, final CampLayout.Tent other) {
    return one.placed().stream()
        .mapToInt(
            a ->
                other.placed().stream()
                    .mapToInt(b -> apart(a.pos(), b.pos()))
                    .min()
                    .orElse(Integer.MAX_VALUE))
        .min()
        .orElse(Integer.MAX_VALUE);
  }

  private static int apart(final BlockPos one, final BlockPos other) {
    return Math.max(Math.abs(one.getX() - other.getX()), Math.abs(one.getZ() - other.getZ()));
  }
}
