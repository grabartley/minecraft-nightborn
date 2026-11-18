package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.config.SunSettings;
import com.grahambartley.nightborn.smoke.SmokeParticles;
import com.grahambartley.nightborn.sound.NightbornSounds;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public final class SunBurn {

  static final int BURN_INTERVAL_TICKS = 20;
  static final float FIRE_SECONDS = 8.0F;
  static final int SMOKE_COUNT = 8;

  private SunBurn() {}

  public static float damage(final float sunDamage, final double sunmarked) {
    return (float) (sunDamage * sunmarked);
  }

  public static boolean tick(final LivingEntity vampire, final SunSettings settings) {
    if (!(vampire.getWorld() instanceof ServerWorld world) || !DirectSun.on(vampire)) {
      return false;
    }
    if (vampire.age % BURN_INTERVAL_TICKS != 0) {
      return true;
    }
    vampire.setOnFireFor(FIRE_SECONDS);
    world.spawnParticles(
        SmokeParticles.VAMPIRE_SMOKE,
        vampire.getX(),
        vampire.getBodyY(0.6),
        vampire.getZ(),
        SMOKE_COUNT,
        vampire.getWidth() / 2.0,
        vampire.getHeight() / 3.0,
        vampire.getWidth() / 2.0,
        0.01);
    world.playSound(
        null,
        vampire.getBlockPos(),
        NightbornSounds.SIZZLE,
        vampire.getSoundCategory(),
        0.7F,
        NightbornSounds.variedPitch(vampire.getRandom()));
    if (vampire.damage(
            NightbornDamage.sun(world),
            damage(settings.sunDamage(), WornAttributes.value(vampire, WornAttributes.SUN_DAMAGE)))
        && vampire.isAlive()) {
      MilestoneCriterion.reach(vampire, Milestone.SUN_SURVIVED);
    }
    return true;
  }
}
