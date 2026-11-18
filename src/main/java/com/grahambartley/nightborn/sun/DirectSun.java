package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.coffin.CoffinSleepers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DirectSun {

  static final float BRIGHT_ENOUGH_TO_BURN = 0.5F;

  private DirectSun() {}

  public static boolean exposed(
      final boolean day,
      final boolean openSky,
      final boolean wet,
      final boolean inPowderSnow,
      final boolean inClosedCoffin) {
    return day && openSky && !wet && !inPowderSnow && !inClosedCoffin;
  }

  public static boolean burns(
      final boolean day,
      final float brightnessAtEyes,
      final boolean openSky,
      final boolean wet,
      final boolean inPowderSnow,
      final boolean inClosedCoffin) {
    return brightnessAtEyes > BRIGHT_ENOUGH_TO_BURN
        && exposed(day, openSky, wet, inPowderSnow, inClosedCoffin);
  }

  public static boolean shaft(
      final boolean lit,
      final boolean wet,
      final boolean inPowderSnow,
      final boolean inClosedCoffin) {
    return lit && exposed(true, true, wet, inPowderSnow, inClosedCoffin);
  }

  public static boolean on(final LivingEntity entity) {
    if (shaft(
        SunShafts.lit(entity),
        entity.isWet(),
        inPowderSnow(entity),
        CoffinSleepers.shelteredBy(entity))) {
      return true;
    }
    return burns(
        entity.getWorld().isDay(),
        entity.getBrightnessAtEyes(),
        openSkyAbove(entity),
        entity.isWet(),
        inPowderSnow(entity),
        CoffinSleepers.shelteredBy(entity));
  }

  public static boolean under(final LivingEntity entity, final boolean sunUp) {
    return exposed(
        sunUp,
        openSkyAbove(entity),
        entity.isWet(),
        inPowderSnow(entity),
        CoffinSleepers.shelteredBy(entity));
  }

  public static boolean openSkyAbove(final LivingEntity entity) {
    final World world = entity.getWorld();
    return world.isSkyVisible(BlockPos.ofFloored(entity.getX(), entity.getEyeY(), entity.getZ()));
  }

  private static boolean inPowderSnow(final LivingEntity entity) {
    return entity.inPowderSnow || entity.wasInPowderSnow;
  }
}
