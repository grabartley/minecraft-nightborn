package com.grahambartley.nightborn.structure;

import java.util.List;
import java.util.Optional;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;

public final class StructureHeart {

  private StructureHeart() {}

  public static Optional<BlockBox> of(final List<StructurePiece> pieces) {
    return pieces.isEmpty() ? Optional.empty() : Optional.of(pieces.getFirst().getBoundingBox());
  }
}
