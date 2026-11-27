package com.grahambartley.nightborn.structure;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

final class FakeRock implements CaveMouth.Ground {

  private final Map<BlockPos, CaveCell> cells = new HashMap<>();
  private final CaveCell rest;

  FakeRock(final CaveCell rest) {
    this.rest = rest;
  }

  FakeRock fill(final BlockBox box, final CaveCell cell) {
    BlockPos.stream(box).forEach(pos -> cells.put(pos.toImmutable(), cell));
    return this;
  }

  FakeRock cave(final int x0, final int floorY, final int z0, final int x1, final int z1) {
    return fill(new BlockBox(x0, floorY + 1, z0, x1, floorY + 3, z1), CaveCell.OPEN);
  }

  @Override
  public CaveCell at(final BlockPos pos) {
    return cells.getOrDefault(pos, rest);
  }
}
