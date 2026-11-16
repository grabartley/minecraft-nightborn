package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.vampire.Impulse;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
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

  public Vec3d push(final Vec3d toward) {
    return Impulse.push(Impulse.ahead(toward), forward, up);
  }

  public void launch(final LivingEntity caster, final Vec3d toward) {
    caster.setVelocity(caster.getVelocity().multiply(1.0, 0.0, 1.0).add(push(toward)));
    caster.velocityModified = true;
    caster.fallDistance = 0.0F;
  }
}
