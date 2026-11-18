package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;

public final class CampBats {

  public static final int SPREAD = 6;
  public static final int RISE = 6;
  public static final double RAID_RANGE = 32.0;

  private CampBats() {}

  public static void raid(
      final ServerWorld world, final BlockPos fire, final int count, final Random random) {
    for (int i = 0; i < count; i++) {
      final VampireBatEntity bat = NightbornMobs.VAMPIRE_BAT.create(world);
      if (bat == null) {
        return;
      }
      final BlockPos at =
          fire.add(
              random.nextBetween(-SPREAD, SPREAD),
              RISE + random.nextInt(4),
              random.nextBetween(-SPREAD, SPREAD));
      bat.refreshPositionAndAngles(at, random.nextFloat() * 360.0F, 0.0F);
      bat.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
      bat.raidCamp();
      world.spawnEntity(bat);
    }
  }

  public static int raidersNear(final ServerWorld world, final BlockPos fire) {
    return world
        .getEntitiesByClass(
            VampireBatEntity.class,
            new Box(fire).expand(RAID_RANGE),
            bat -> bat.raidsCamp() && bat.isAlive())
        .size();
  }
}
