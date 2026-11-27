package com.grahambartley.nightborn.structure;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;

public final class StructureEntrance {

  private StructureEntrance() {}

  public static Optional<BlockPos> found(
      final Optional<Boolean> saved,
      final Supplier<Optional<BlockPos>> built,
      final Supplier<Optional<BlockPos>> planned,
      final BlockPos locate) {
    if (saved.isEmpty()) {
      return planned.get();
    }
    return saved.get() ? Optional.of(built.get().or(planned).orElse(locate)) : Optional.empty();
  }
}
