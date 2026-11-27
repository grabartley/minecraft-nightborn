package com.grahambartley.nightborn.structure;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;

public final class StructureEntrance {

  private StructureEntrance() {}

  public static BlockPos standingSpot(final Threshold way) {
    return way.floor().offset(way.out()).up();
  }

  public static Optional<BlockPos> found(
      final Optional<Boolean> saved,
      final Supplier<Optional<BlockPos>> planned,
      final BlockPos locate) {
    if (saved.isPresent() && !saved.get()) {
      return Optional.empty();
    }
    final Optional<BlockPos> plan = planned.get();
    return saved.isPresent() ? Optional.of(plan.orElse(locate)) : plan;
  }
}
