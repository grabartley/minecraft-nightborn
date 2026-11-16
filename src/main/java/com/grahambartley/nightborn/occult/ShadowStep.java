package com.grahambartley.nightborn.occult;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class ShadowStep {

  static final int FLOOR_SEARCH = 3;

  private ShadowStep() {}

  public static Optional<Vec3d> destination(
      final LivingEntity caster, final ShadowStepPower power) {
    final Vec3d from = caster.getEyePos();
    final Vec3d wanted = wanted(caster, power);
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
      final Vec3d behind =
          ShadowStepPath.behind(caster.getPos(), mob.getTarget().getPos(), power.behind());
      final Vec3d eyeHeight = new Vec3d(0.0, caster.getStandingEyeHeight(), 0.0);
      final Vec3d way = behind.add(eyeHeight).subtract(caster.getEyePos());
      return way.length() > power.range()
          ? caster.getEyePos().add(way.normalize().multiply(power.range()))
          : behind.add(eyeHeight);
    }
    return caster.getEyePos().add(caster.getRotationVector().multiply(power.range()));
  }

  private static Optional<Vec3d> standing(final LivingEntity caster, final Vec3d eye) {
    final double feetY = eye.y - caster.getStandingEyeHeight();
    final BlockPos start = BlockPos.ofFloored(eye.x, feetY, eye.z);
    for (int down = 0; down <= FLOOR_SEARCH; down++) {
      final BlockPos feet = start.down(down);
      final Vec3d at = new Vec3d(eye.x, feet.getY(), eye.z);
      if (caster.getWorld().getBlockState(feet.down()).isSolidBlock(caster.getWorld(), feet.down())
          && caster
              .getWorld()
              .isSpaceEmpty(caster, caster.getDimensions(caster.getPose()).getBoxAt(at))
          && !caster
              .getWorld()
              .containsFluid(caster.getDimensions(caster.getPose()).getBoxAt(at))) {
        return Optional.of(at);
      }
    }
    return Optional.empty();
  }

  public static void step(final LivingEntity caster, final Vec3d to) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    puff(world, caster.getPos(), caster.getHeight());
    caster.teleport(
        world,
        to.x,
        to.y,
        to.z,
        EnumSet.noneOf(PositionFlag.class),
        caster.getYaw(),
        caster.getPitch());
    caster.fallDistance = 0.0F;
    puff(world, to, caster.getHeight());
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
