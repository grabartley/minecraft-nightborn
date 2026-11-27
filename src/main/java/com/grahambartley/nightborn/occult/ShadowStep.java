package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.skill.CastAim;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public final class ShadowStep {

  private ShadowStep() {}

  public static Optional<Vec3d> destination(
      final LivingEntity caster, final ShadowStepPower power, final CastAim aim) {
    return Landing.reach(caster, wanted(caster, power, aim));
  }

  private static Vec3d wanted(
      final LivingEntity caster, final ShadowStepPower power, final CastAim aim) {
    if (caster instanceof MobEntity && aim.spot() != null) {
      return ShadowStepPath.toward(
          caster.getPos(), caster.getStandingEyeHeight(), aim.spot(), power.range());
    }
    final LivingEntity subject = aim.subject(caster);
    if (caster instanceof MobEntity && subject != null) {
      return ShadowStepPath.aimBehind(
          caster.getPos(),
          caster.getStandingEyeHeight(),
          subject.getPos(),
          power.behind(),
          power.range());
    }
    return caster.getEyePos().add(caster.getRotationVector().multiply(power.range()));
  }

  public static void step(final LivingEntity caster, final Vec3d to) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    puff(world, caster.getPos(), caster.getHeight());
    Landing.move(caster, to);
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
