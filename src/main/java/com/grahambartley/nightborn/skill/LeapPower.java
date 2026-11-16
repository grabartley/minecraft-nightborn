package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record LeapPower(float forward, float up) {

  public static final Codec<LeapPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, 4.0F).fieldOf("forward").forGetter(LeapPower::forward),
                      Codec.floatRange(0.0F, 2.0F).fieldOf("up").forGetter(LeapPower::up))
                  .apply(instance, LeapPower::new));

  public Vec3d push(final float yaw) {
    final float radians = yaw * MathHelper.RADIANS_PER_DEGREE;
    return new Vec3d(-MathHelper.sin(radians) * forward, up, MathHelper.cos(radians) * forward);
  }

  public void launch(final LivingEntity caster) {
    final Vec3d push = push(caster.getYaw());
    caster.setVelocity(caster.getVelocity().multiply(1.0, 0.0, 1.0).add(push));
    caster.velocityModified = true;
    caster.fallDistance = 0.0F;
  }
}
