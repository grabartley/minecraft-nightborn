package com.grahambartley.nightborn.guild.party;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public enum TentPart implements StringIdentifiable {
  LEFT_FRONT("left_front", -1, -1, -1),
  LEFT_MIDDLE("left_middle", -1, -1, 0),
  LEFT_BACK("left_back", -1, -1, 1),
  RIGHT_FRONT("right_front", 1, -1, -1),
  RIGHT_MIDDLE("right_middle", 1, -1, 0),
  RIGHT_BACK("right_back", 1, -1, 1),
  BACK("back", 0, -1, 1),
  ROOF_FRONT("roof_front", 0, 0, -1),
  ROOF_MIDDLE("roof_middle", 0, 0, 0),
  ROOF_BACK("roof_back", 0, 0, 1);

  public static final TentPart ORIGIN = ROOF_MIDDLE;

  private final String name;
  private final int right;
  private final int up;
  private final int back;

  TentPart(final String name, final int right, final int up, final int back) {
    this.name = name;
    this.right = right;
    this.up = up;
    this.back = back;
  }

  public int right() {
    return right;
  }

  public boolean roof() {
    return up == 0;
  }

  public TentPart mirrored() {
    return switch (this) {
      case LEFT_FRONT -> RIGHT_FRONT;
      case LEFT_MIDDLE -> RIGHT_MIDDLE;
      case LEFT_BACK -> RIGHT_BACK;
      case RIGHT_FRONT -> LEFT_FRONT;
      case RIGHT_MIDDLE -> LEFT_MIDDLE;
      case RIGHT_BACK -> LEFT_BACK;
      default -> this;
    };
  }

  public BlockPos offset(final Direction facing) {
    return BlockPos.ORIGIN
        .offset(facing.rotateYClockwise(), right)
        .up(up)
        .offset(facing.getOpposite(), back);
  }

  public BlockPos origin(final BlockPos pos, final Direction facing) {
    return pos.subtract(offset(facing));
  }

  @Override
  public String asString() {
    return name;
  }
}
