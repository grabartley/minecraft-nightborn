package com.grahambartley.nightborn.structure;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.structure.Structure;

public final class NoiseGround implements CaveMouth.Ground {

  private static final int CHUNK = 16;

  private final Structure.Context context;
  private final NoiseChunkGenerator generator;
  private final ChunkGeneratorSettings settings;
  private final GenerationShapeConfig shape;
  private final DepthBand band;
  private final Map<Long, BlockState[]> chunks = new HashMap<>();

  public NoiseGround(
      final Structure.Context context, final NoiseChunkGenerator generator, final DepthBand depth) {
    this.context = context;
    this.generator = generator;
    this.settings = generator.getSettings().value();
    this.shape = settings.generationShapeConfig().trimHeight(context.world());
    this.band = depth.within(shape.minimumY(), shape.minimumY() + shape.height());
  }

  @Override
  public CaveCell at(final BlockPos pos) {
    final int y = pos.getY() - band.bottom();
    if (y < 0 || y >= band.height()) {
      return CaveCell.SOLID;
    }
    final BlockState[] states =
        chunks.computeIfAbsent(
            ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4),
            packed -> fill(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4)));
    return CaveCell.of(states[index(pos.getX() & 15, y, pos.getZ() & 15)]);
  }

  private int index(final int x, final int y, final int z) {
    return (y * CHUNK + z) * CHUNK + x;
  }

  private BlockState[] fill(final ChunkPos chunk) {
    final int cellWidth = shape.horizontalCellBlockCount();
    final int cellHeight = shape.verticalCellBlockCount();
    final int cells = CHUNK / cellWidth;
    final int lowestCell = Math.floorDiv(shape.minimumY(), cellHeight);
    final int firstLayer = Math.floorDiv(band.bottom(), cellHeight) - lowestCell;
    final int lastLayer = Math.floorDiv(band.top() - 1, cellHeight) - lowestCell;
    final BlockState[] states = new BlockState[CHUNK * CHUNK * band.height()];
    final ChunkNoiseSampler sampler =
        new ChunkNoiseSampler(
            cells,
            context.noiseConfig(),
            chunk.getStartX(),
            chunk.getStartZ(),
            shape,
            DensityFunctionTypes.Beardifier.INSTANCE,
            settings,
            generator.fluidLevelSampler.get(),
            Blender.getNoBlending());
    sampler.sampleStartDensity();
    for (int cellX = 0; cellX < cells; cellX++) {
      sampler.sampleEndDensity(cellX);
      for (int cellZ = 0; cellZ < cells; cellZ++) {
        for (int cellY = lastLayer; cellY >= firstLayer; cellY--) {
          sampler.onSampledCellCorners(cellY, cellZ);
          for (int localY = cellHeight - 1; localY >= 0; localY--) {
            final int blockY = (lowestCell + cellY) * cellHeight + localY;
            sampler.interpolateY(blockY, (double) localY / cellHeight);
            for (int localX = 0; localX < cellWidth; localX++) {
              final int x = cellX * cellWidth + localX;
              sampler.interpolateX(chunk.getStartX() + x, (double) localX / cellWidth);
              for (int localZ = 0; localZ < cellWidth; localZ++) {
                final int z = cellZ * cellWidth + localZ;
                sampler.interpolateZ(chunk.getStartZ() + z, (double) localZ / cellWidth);
                final BlockState state = sampler.sampleBlockState();
                if (blockY >= band.bottom() && blockY < band.top()) {
                  states[index(x, blockY - band.bottom(), z)] =
                      state == null ? settings.defaultBlock() : state;
                }
              }
            }
          }
        }
      }
      sampler.swapBuffers();
    }
    sampler.stopInterpolation();
    return states;
  }
}
