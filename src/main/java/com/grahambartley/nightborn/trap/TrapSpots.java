package com.grahambartley.nightborn.trap;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class TrapSpots {

  private TrapSpots() {}

  public static boolean fits(
      final boolean floorSolid, final boolean spaceOpen, final boolean wet, final boolean burning) {
    return floorSolid && spaceOpen && !wet && !burning;
  }

  public static boolean inRange(final double squaredDistance, final float range) {
    return squaredDistance <= (double) range * range;
  }

  public static Optional<BlockPos> spot(final LivingEntity caster, final float range) {
    final BlockPos at = aimed(caster, range);
    if (at == null
        || !inRange(caster.squaredDistanceTo(at.toBottomCenterPos()), range + 1.0F)
        || !fits(caster.getWorld(), at)) {
      return Optional.empty();
    }
    return Optional.of(at);
  }

  public static boolean fits(final World world, final BlockPos at) {
    final BlockState space = world.getBlockState(at);
    return fits(
        world.getBlockState(at.down()).isSideSolidFullSquare(world, at.down(), Direction.UP),
        space.getCollisionShape(world, at).isEmpty(),
        !space.getFluidState().isEmpty(),
        space.isIn(BlockTags.FIRE));
  }

  private static BlockPos aimed(final LivingEntity caster, final float range) {
    if (caster instanceof MobEntity mob) {
      return mob.getTarget() == null ? null : mob.getTarget().getBlockPos();
    }
    final HitResult hit = caster.raycast(range, 1.0F, false);
    if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) {
      return null;
    }
    return block.getSide() == Direction.UP
        ? block.getBlockPos().up()
        : block.getBlockPos().offset(block.getSide());
  }
}
