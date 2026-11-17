package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import java.util.Comparator;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

public enum HeadSeat implements StringIdentifiable {
  LOWEST("lowest", Comparator.comparingInt(BlockPos::getY)),
  HIGHEST("highest", Comparator.comparingInt(BlockPos::getY).reversed());

  public static final Codec<HeadSeat> CODEC = StringIdentifiable.createCodec(HeadSeat::values);

  private final String id;
  private final Comparator<BlockPos> order;

  HeadSeat(final String id, final Comparator<BlockPos> order) {
    this.id = id;
    this.order = order;
  }

  public Comparator<BlockPos> order() {
    return order;
  }

  @Override
  public String asString() {
    return id;
  }
}
