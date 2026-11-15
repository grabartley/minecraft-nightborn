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
    final World world = entity.getWorld();
    return exposed(
        world.isDay(),
        world.isSkyVisible(BlockPos.ofFloored(entity.getX(), entity.getEyeY(), entity.getZ())),
        entity.isWet(),
        entity.inPowderSnow || entity.wasInPowderSnow,
        CoffinSleepers.shelteredBy(entity));
  }
}
