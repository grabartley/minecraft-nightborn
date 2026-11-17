package com.grahambartley.nightborn.structure;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.JigsawBlock;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.Direction;

public record Threshold(BlockPos floor, Direction out) {

  static final String NAME_KEY = "name";

  public ColumnPos outside() {
    final BlockPos step = floor.offset(out);
    return new ColumnPos(step.getX(), step.getZ());
  }

  public int lift(final int surface) {
    return surface - 1 - floor.getY();
  }

  public static Optional<Threshold> named(
      final Identifier name, final List<StructureTemplate.StructureBlockInfo> jigsaws) {
    return jigsaws.stream()
        .filter(
            jigsaw ->
                jigsaw.nbt() != null && name.toString().equals(jigsaw.nbt().getString(NAME_KEY)))
        .findFirst()
        .map(jigsaw -> new Threshold(jigsaw.pos(), JigsawBlock.getFacing(jigsaw.state())));
  }
}
