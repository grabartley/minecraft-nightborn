package com.grahambartley.nightborn.occult;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public final class ShadowStep {

  static final int FLOOR_SEARCH = 3;

  private ShadowStep() {}

  public static Optional<Vec3d> destination(
      final LivingEntity caster, final ShadowStepPower power) {
    return reach(caster, wanted(caster, power));
  }

  public static Optional<Vec3d> reach(final LivingEntity caster, final Vec3d wanted) {
    final Vec3d from = caster.getEyePos();
    final HitResult hit =
        caster
            .getWorld()
            .raycast(
                new RaycastContext(
                    from,
                    wanted,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    caster));
    final Vec3d reach = hit.getType() == HitResult.Type.MISS ? wanted : hit.getPos();
    for (final Vec3d point : ShadowStepPath.candidates(from, reach)) {
      final Optional<Vec3d> feet = standing(caster, point);
      if (feet.isPresent()) {
        return feet;
      }
    }
    return Optional.empty();
  }

  private static Vec3d wanted(final LivingEntity caster, final ShadowStepPower power) {
    if (caster instanceof MobEntity mob && mob.getTarget() != null) {
      return ShadowStepPath.aimBehind(
          caster.getPos(),
          caster.getStandingEyeHeight(),
          mob.getTarget().getPos(),
          power.behind(),
          power.range());
    }
    return caster.getEyePos().add(caster.getRotationVector().multiply(power.range()));
  }

  private static Optional<Vec3d> standing(final LivingEntity caster, final Vec3d eye) {
    final double feetY = eye.y - caster.getStandingEyeHeight();
    final BlockPos start = BlockPos.ofFloored(eye.x, feetY, eye.z);
    for (int down = 0; down <= FLOOR_SEARCH; down++) {
      final BlockPos floor = start.down(down);
      final VoxelShape shape =
          caster.getWorld().getBlockState(floor).getCollisionShape(caster.getWorld(), floor);
      if (shape.isEmpty()) {
        continue;
      }
      final Vec3d at = new Vec3d(eye.x, floor.getY() + shape.getMax(Direction.Axis.Y), eye.z);
      final Box body = caster.getDimensions(caster.getPose()).getBoxAt(at);
      if (caster.getWorld().isSpaceEmpty(caster, body)
          && !caster.getWorld().containsFluid(body)
          && !burns(caster.getWorld(), body, floor)) {
        return Optional.of(at);
      }
      return Optional.empty();
    }
    return Optional.empty();
  }

  private static boolean burns(final World world, final Box body, final BlockPos floor) {
    final BlockState under = world.getBlockState(floor);
    return under.isOf(Blocks.MAGMA_BLOCK)
        || CampfireBlock.isLitCampfire(under)
        || world
            .getStatesInBox(body)
            .anyMatch(state -> state.isIn(BlockTags.FIRE) || CampfireBlock.isLitCampfire(state));
  }

  public static void step(final LivingEntity caster, final Vec3d to) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    puff(world, caster.getPos(), caster.getHeight());
    move(caster, to);
    puff(world, to, caster.getHeight());
  }

  public static void move(final LivingEntity caster, final Vec3d to) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    caster.teleport(
        world,
        to.x,
        to.y,
        to.z,
        EnumSet.noneOf(PositionFlag.class),
        caster.getYaw(),
        caster.getPitch());
    caster.fallDistance = 0.0F;
  }

  private static void puff(final ServerWorld world, final Vec3d at, final float height) {
    world.spawnParticles(
        ParticleTypes.LARGE_SMOKE,
        at.x,
        at.y + height / 2.0,
        at.z,
        16,
        0.3,
        height / 3.0,
        0.3,
        0.01);
    world.spawnParticles(
        ParticleTypes.SQUID_INK,
        at.x,
        at.y + height / 2.0,
        at.z,
        8,
        0.25,
        height / 3.0,
        0.25,
        0.02);
  }
}
