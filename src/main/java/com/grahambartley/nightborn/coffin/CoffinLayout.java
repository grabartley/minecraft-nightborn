package com.grahambartley.nightborn.coffin;

import java.util.List;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class CoffinLayout {

  static final double BODY_LIFT = 5.875 / 16.0;
  static final double HEAD_SHIFT = 5.32 / 16.0;
  static final double STANDING_FLOOR = 0.5 / 16.0;
  static final float INNER_LENGTH = 31.5F / 16.0F;
  static final float UPRIGHT_SLEEPER_WIDTH = 0.2F;

  private CoffinLayout() {}

  public static Direction towardsOtherPart(
      final BedPart part, final Direction facing, final boolean standing) {
    if (standing) {
      return part == BedPart.FOOT ? Direction.UP : Direction.DOWN;
    }
    return part == BedPart.FOOT ? facing : facing.getOpposite();
  }

  public static BlockPos head(
      final BlockPos pos, final BedPart part, final Direction facing, final boolean standing) {
    return part == BedPart.HEAD ? pos : pos.offset(towardsOtherPart(part, facing, standing));
  }

  public static BlockPos foot(
      final BlockPos pos, final BedPart part, final Direction facing, final boolean standing) {
    return part == BedPart.FOOT ? pos : pos.offset(towardsOtherPart(part, facing, standing));
  }

  public static EntityDimensions uprightSleeper(final EntityDimensions standing) {
    return EntityDimensions.fixed(UPRIGHT_SLEEPER_WIDTH, Math.min(standing.height(), INNER_LENGTH))
        .withEyeHeight(Math.min(standing.eyeHeight(), INNER_LENGTH));
  }

  public static float outwardYaw(final Direction facing) {
    return facing.getOpposite().asRotation();
  }

  public static Vec3d restingSpot(
      final BlockPos head, final Direction facing, final boolean standing) {
    if (!standing) {
      return Vec3d.add(
          head,
          0.5 + facing.getOffsetX() * HEAD_SHIFT,
          BODY_LIFT,
          0.5 + facing.getOffsetZ() * HEAD_SHIFT);
    }
    final double towardsBack = 0.5 - BODY_LIFT;
    return Vec3d.add(
        head.down(),
        0.5 + facing.getOffsetX() * towardsBack,
        STANDING_FLOOR,
        0.5 + facing.getOffsetZ() * towardsBack);
  }

  public static List<BlockPos> frontCells(final BlockPos head, final Direction facing) {
    final Direction out = facing.getOpposite();
    return List.of(head.offset(out), head.down().offset(out));
  }

  public static List<BlockPos> stepOutCells(final BlockPos head, final Direction facing) {
    final BlockPos foot = head.down();
    final Direction out = facing.getOpposite();
    final BlockPos ahead = foot.offset(out);
    final Direction left = out.rotateYCounterclockwise();
    final Direction right = out.rotateYClockwise();
    return List.of(
        ahead,
        ahead.offset(left),
        ahead.offset(right),
        foot.offset(left),
        foot.offset(right),
        ahead.offset(out));
  }
}
