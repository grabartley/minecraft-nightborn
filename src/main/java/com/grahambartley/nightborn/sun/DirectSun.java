package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.coffin.CoffinSleepers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DirectSun {

  private DirectSun() {}

  public static boolean exposed(
      final boolean day,
      final boolean openSky,
      final boolean wet,
      final boolean inPowderSnow,
      final boolean inClosedCoffin) {
    return day && openSky && !wet && !inPowderSnow && !inClosedCoffin;
  }

  public static boolean on(final LivingEntity entity) {
    return under(entity, entity.getWorld().isDay());
  }

  public static boolean under(final LivingEntity entity, final boolean sunUp) {
    return exposed(
        sunUp,
        openSkyAbove(entity),
        entity.isWet(),
        entity.inPowderSnow || entity.wasInPowderSnow,
        CoffinSleepers.shelteredBy(entity));
  }

  public static boolean openSkyAbove(final LivingEntity entity) {
    final World world = entity.getWorld();
    return world.isSkyVisible(BlockPos.ofFloored(entity.getX(), entity.getEyeY(), entity.getZ()));
  }
}
