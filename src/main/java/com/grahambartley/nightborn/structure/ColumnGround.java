package com.grahambartley.nightborn.structure;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.structure.Structure;

public final class ColumnGround implements CaveMouth.Ground {

  private final Structure.Context context;
  private final Map<Long, VerticalBlockSample> columns = new HashMap<>();

  public ColumnGround(final Structure.Context context) {
    this.context = context;
  }

  @Override
  public CaveCell at(final BlockPos pos) {
    final VerticalBlockSample column =
        columns.computeIfAbsent(
            ColumnPos.pack(pos.getX(), pos.getZ()),
            packed ->
                context
                    .chunkGenerator()
                    .getColumnSample(
                        pos.getX(), pos.getZ(), context.world(), context.noiseConfig()));
    return CaveCell.of(column.getState(pos.getY()));
  }
}
