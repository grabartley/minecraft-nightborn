package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DrainAttack(
    int ticks, int everyTicks, float damage, float heal, float range, float breakDamage) {

  public static final Codec<DrainAttack> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 1200).fieldOf("ticks").forGetter(DrainAttack::ticks),
                      Codec.intRange(1, 200)
                          .optionalFieldOf("every_ticks", 10)
                          .forGetter(DrainAttack::everyTicks),
                      Codec.floatRange(0.0F, 200.0F)
                          .fieldOf("damage")
                          .forGetter(DrainAttack::damage),
                      Codec.floatRange(0.0F, 1000.0F).fieldOf("heal").forGetter(DrainAttack::heal),
                      Codec.floatRange(1.0F, 64.0F)
                          .optionalFieldOf("range", 16.0F)
                          .forGetter(DrainAttack::range),
                      Codec.floatRange(0.0F, 1000.0F)
                          .fieldOf("break_damage")
                          .forGetter(DrainAttack::breakDamage))
                  .apply(instance, DrainAttack::new));

  public enum Step {
    HOLD,
    PULSE,
    BROKEN,
    DONE
  }

  public Step step(
      final int tick, final float taken, final boolean foeAlive, final double distance) {
    if (taken >= breakDamage) {
      return Step.BROKEN;
    }
    if (tick >= ticks || !foeAlive || distance > range) {
      return Step.DONE;
    }
    return tick > 0 && tick % everyTicks == 0 ? Step.PULSE : Step.HOLD;
  }

  public static float healed(final float health, final float heal, final float ceiling) {
    return Math.max(health, Math.min(ceiling, health + heal));
  }
}
