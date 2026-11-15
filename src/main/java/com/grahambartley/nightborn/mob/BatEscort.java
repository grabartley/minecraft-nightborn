package com.grahambartley.nightborn.mob;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

public final class BatEscort {

  static final double HOVER_HEIGHT = 1.5;

  private BatEscort() {}

  public static int gather(
      final ServerWorldAccess world, final MobEntity leader, final int chance) {
    final Random random = world.getRandom();
    final int size =
        MobSpawnRules.escortSize(
            random.nextInt(100), chance, random.nextInt(MobSpawnRules.MAX_ESCORT));
    int gathered = 0;
    for (int i = 0; i < size; i++) {
      final VampireBatEntity bat = NightbornMobs.VAMPIRE_BAT.create(world.toServerWorld());
      if (bat == null) {
        continue;
      }
      bat.refreshPositionAndAngles(
          leader.getX() + random.nextDouble() - 0.5,
          leader.getY() + HOVER_HEIGHT,
          leader.getZ() + random.nextDouble() - 0.5,
          random.nextFloat() * 360.0F,
          0.0F);
      if (!world.isSpaceEmpty(bat)) {
        bat.discard();
        continue;
      }
      bat.initialize(
          world, world.getLocalDifficulty(bat.getBlockPos()), SpawnReason.REINFORCEMENT, null);
      bat.setRoosting(false);
      world.spawnEntityAndPassengers(bat);
      gathered++;
    }
    return gathered;
  }
}
