package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;

public record Footing(
    boolean dry,
    int maxRise,
    int spacing,
    Optional<Identifier> entrance,
    Optional<Integer> maxLift) {

  public static final int WIDEST_SPACING = 16;
  public static final int STEEPEST_RISE = 64;

  public static final Codec<Footing> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.BOOL.optionalFieldOf("dry", true).forGetter(Footing::dry),
                      Codec.intRange(0, STEEPEST_RISE)
                          .fieldOf("max_rise")
                          .forGetter(Footing::maxRise),
                      Codec.intRange(1, WIDEST_SPACING)
                          .optionalFieldOf("spacing", 8)
                          .forGetter(Footing::spacing),
                      Identifier.CODEC.optionalFieldOf("entrance").forGetter(Footing::entrance),
                      Codec.intRange(0, STEEPEST_RISE)
                          .optionalFieldOf("max_lift")
                          .forGetter(Footing::maxLift))
                  .apply(instance, Footing::new));

  public record Column(int surface, int floor) {

    public boolean underWater() {
      return surface > floor;
    }
  }

  public boolean allowsLift(final int lift) {
    return Math.abs(lift) <= maxLift.orElse(maxRise);
  }

  public boolean accepts(final List<Column> columns) {
    if (columns.isEmpty()) {
      return false;
    }
    int lowest = Integer.MAX_VALUE;
    int highest = Integer.MIN_VALUE;
    for (final Column column : columns) {
      if (dry && column.underWater()) {
        return false;
      }
      lowest = Math.min(lowest, column.floor());
      highest = Math.max(highest, column.floor());
    }
    return highest - lowest <= maxRise;
  }

  public List<ColumnPos> samples(final BlockBox box, final ColumnPos start) {
    final List<ColumnPos> points = new ArrayList<>();
    points.add(start);
    for (final int x : steps(box.getMinX(), box.getMaxX())) {
      for (final int z : steps(box.getMinZ(), box.getMaxZ())) {
        points.add(new ColumnPos(x, z));
      }
    }
    return points;
  }

  public boolean fits(final Structure.Context context, final BlockBox box, final ColumnPos start) {
    final List<Column> columns = new ArrayList<>();
    for (final ColumnPos point : samples(box, start)) {
      columns.add(
          new Column(
              height(context, point, Heightmap.Type.WORLD_SURFACE_WG),
              height(context, point, Heightmap.Type.OCEAN_FLOOR_WG)));
    }
    return accepts(columns);
  }

  private List<Integer> steps(final int min, final int max) {
    final List<Integer> steps = new ArrayList<>();
    for (int v = min; v < max; v += spacing) {
      steps.add(v);
    }
    steps.add(max);
    return steps;
  }

  private static int height(
      final Structure.Context context, final ColumnPos point, final Heightmap.Type type) {
    return context
        .chunkGenerator()
        .getHeight(point.x(), point.z(), type, context.world(), context.noiseConfig());
  }
}
