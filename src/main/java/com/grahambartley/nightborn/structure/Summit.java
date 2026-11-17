package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;

public record Summit(int reach, int spacing, int minHeight, int maxHeight) {

  public static final int FARTHEST_REACH = 64;
  public static final int WIDEST_SPACING = 16;
  static final int LOWEST = -64;
  static final int HIGHEST = 320;

  public static final Codec<Summit> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, FARTHEST_REACH).fieldOf("reach").forGetter(Summit::reach),
                      Codec.intRange(1, WIDEST_SPACING)
                          .optionalFieldOf("spacing", 8)
                          .forGetter(Summit::spacing),
                      Codec.intRange(LOWEST, HIGHEST)
                          .optionalFieldOf("min_height", LOWEST)
                          .forGetter(Summit::minHeight),
                      Codec.intRange(LOWEST, HIGHEST)
                          .optionalFieldOf("max_height", HIGHEST)
                          .forGetter(Summit::maxHeight))
                  .apply(instance, Summit::new));

  public List<ColumnPos> samples(final ColumnPos start) {
    final List<ColumnPos> points = new ArrayList<>();
    for (int dx = -reach; dx <= reach; dx += spacing) {
      for (int dz = -reach; dz <= reach; dz += spacing) {
        points.add(new ColumnPos(start.x() + dx, start.z() + dz));
      }
    }
    if (!points.contains(start)) {
      points.add(start);
    }
    return points;
  }

  public Optional<ColumnPos> peak(final ColumnPos start, final ToIntFunction<ColumnPos> heights) {
    return samples(start).stream()
        .filter(
            point ->
                heights.applyAsInt(point) >= minHeight && heights.applyAsInt(point) <= maxHeight)
        .max(
            Comparator.comparingInt(heights::applyAsInt)
                .thenComparing(
                    Comparator.comparingLong((ColumnPos point) -> distanceSquared(point, start))
                        .reversed())
                .thenComparing(Comparator.comparingInt(ColumnPos::x).reversed())
                .thenComparing(Comparator.comparingInt(ColumnPos::z).reversed()));
  }

  public Optional<ColumnPos> peak(final Structure.Context context, final ColumnPos start) {
    return peak(
        start,
        point ->
            context
                .chunkGenerator()
                .getHeight(
                    point.x(),
                    point.z(),
                    Heightmap.Type.WORLD_SURFACE_WG,
                    context.world(),
                    context.noiseConfig()));
  }

  private static long distanceSquared(final ColumnPos one, final ColumnPos other) {
    final long dx = one.x() - other.x();
    final long dz = one.z() - other.z();
    return dx * dx + dz * dz;
  }
}
