package com.grahambartley.nightborn.coffin;

import java.util.Optional;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

public final class CoffinSleepers {

  private CoffinSleepers() {}

  public static boolean isCoffin(final BlockView world, final BlockPos pos) {
    return world.getBlockState(pos).getBlock() instanceof CoffinBlock;
  }

  public static boolean inCoffin(final LivingEntity sleeper) {
    return sleeper
        .getSleepingPosition()
        .map(pos -> isCoffin(sleeper.getWorld(), pos))
        .orElse(false);
  }

  public static boolean upright(final LivingEntity sleeper) {
    return inStandingCoffin(sleeper) || CoffinRise.rising(sleeper);
  }

  public static EntityPose restingPose(final boolean upright, final EntityPose current) {
    return upright ? EntityPose.STANDING : current;
  }

  public static boolean inStandingCoffin(final LivingEntity sleeper) {
    return sleeper
        .getSleepingPosition()
        .map(pos -> CoffinBlock.isStanding(sleeper.getWorld().getBlockState(pos)))
        .orElse(false);
  }

  public static boolean shelteredBy(final LivingEntity sleeper) {
    return sleeper
        .getSleepingPosition()
        .map(pos -> shelters(sleeper.getWorld().getBlockState(pos)))
        .orElse(false);
  }

  public static boolean shelters(final BlockState state) {
    return state.getBlock() instanceof CoffinBlock && !state.get(CoffinBlock.OPEN);
  }

  public static Optional<Vec3d> restingSpot(final BlockView world, final BlockPos head) {
    final BlockState state = world.getBlockState(head);
    if (!(state.getBlock() instanceof CoffinBlock)) {
      return Optional.empty();
    }
    return Optional.of(
        CoffinLayout.restingSpot(
            head, state.get(BedBlock.FACING), state.get(CoffinBlock.STANDING)));
  }

  public static boolean blockedInFront(final BlockView world, final BlockPos head) {
    final BlockState state = world.getBlockState(head);
    return CoffinBlock.isStanding(state)
        && CoffinLayout.frontCells(head, state.get(BedBlock.FACING)).stream()
            .anyMatch(cell -> world.getBlockState(cell).shouldSuffocate(world, cell));
  }
}
