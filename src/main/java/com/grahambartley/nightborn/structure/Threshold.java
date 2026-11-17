package com.grahambartley.nightborn.structure;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public record Threshold(BlockPos floor, Direction out) {

  public ColumnPos outside() {
    final BlockPos step = floor.offset(out);
    return new ColumnPos(step.getX(), step.getZ());
  }

  public BlockPos moveOnto(final ColumnPos ground, final int surface) {
    final ColumnPos outside = outside();
    return new BlockPos(ground.x() - outside.x(), lift(surface), ground.z() - outside.z());
  }

  public int lift(final int surface) {
    return surface - 1 - floor.getY();
  }

  public static Optional<Threshold> named(
      final Identifier name, final List<StructureTemplate.StructureBlockInfo> jigsaws) {
    return jigsaws.stream()
        .filter(
            jigsaw ->
                jigsaw.nbt() != null
                    && name.toString().equals(jigsaw.nbt().getString(JigsawBlockEntity.NAME_KEY)))
        .findFirst()
        .map(jigsaw -> new Threshold(jigsaw.pos(), JigsawBlock.getFacing(jigsaw.state())));
  }

  public static Optional<Threshold> in(
      final List<StructurePiece> pieces,
      final Identifier name,
      final StructureTemplateManager templates,
      final Random random) {
    return pieces.stream()
        .filter(PoolStructurePiece.class::isInstance)
        .map(PoolStructurePiece.class::cast)
        .map(
            piece ->
                named(
                    name,
                    piece
                        .getPoolElement()
                        .getStructureBlockInfos(
                            templates, piece.getPos(), piece.getRotation(), random)))
        .flatMap(Optional::stream)
        .findFirst();
  }
}
