package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.coffin.CoffinSleepers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DirectSun {

  static final float BRIGHT_ENOUGH_TO_BURN = 0.5F;

  private DirectSun() {}

  public static boolean exposed(
      final boolean day, final boolean openSky, final boolean wet, final boolean inClosedCoffin) {
    return day && openSky && !wet && !inClosedCoffin;
  }

  public static boolean burns(
      final boolean day,
      final float brightnessAtEyes,
      final boolean openSky,
      final boolean wet,
      final boolean inClosedCoffin) {
    return brightnessAtEyes > BRIGHT_ENOUGH_TO_BURN && exposed(day, openSky, wet, inClosedCoffin);
  }

  public static boolean on(final LivingEntity entity) {
    return burns(
        entity.getWorld().isDay(),
        entity.getBrightnessAtEyes(),
        openSkyAbove(entity),
        entity.isWet(),
        CoffinSleepers.shelteredBy(entity));
  }

  public static boolean under(final LivingEntity entity, final boolean sunUp) {
    return exposed(sunUp, openSkyAbove(entity), entity.isWet(), CoffinSleepers.shelteredBy(entity));
  }

  public static boolean openSkyAbove(final LivingEntity entity) {
    final World world = entity.getWorld();
    return world.isSkyVisible(BlockPos.ofFloored(entity.getX(), entity.getEyeY(), entity.getZ()));
  }
}
