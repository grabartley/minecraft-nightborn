package com.grahambartley.nightborn.raid;

import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.config.RaidSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public final class HouseNoise {

  static final NoiseLevels LEVELS = new NoiseLevels();

  private HouseNoise() {}

  public static void make(final ServerPlayerEntity maker, final NoiseAct act) {
    if (maker.isSpectator()
        || maker.isCreative()
        || !maker.isAlive()
        || Vampires.isVampire(maker)) {
      return;
    }
    final ServerWorld world = maker.getServerWorld();
    RaidData.noise(world.getRegistryManager(), act)
        .filter(kind -> !kind.silent())
        .ifPresent(kind -> carry(world, maker, kind));
  }

  static void settle(final long now) {
    LEVELS.settle(now, ServerConfigService.current().raids().noiseFade());
  }

  static void forget(final LivingEntity sleeper) {
    LEVELS.forget(sleeper.getUuid());
  }

  static void clear() {
    LEVELS.clear();
  }

  private static void carry(
      final ServerWorld world, final ServerPlayerEntity maker, final NoiseKind kind) {
    final RaidSettings settings = ServerConfigService.current().raids();
    final long now = world.getTime();
    for (final LivingEntity sleeper : sleepers(world, maker, kind.reach())) {
      final double level =
          LEVELS.hear(sleeper.getUuid(), kind.loudness(), now, settings.noiseFade());
      if (Noise.wakes(level, settings.wakeNoise())) {
        HouseAlarm.wake(world, sleeper, maker);
      }
    }
  }

  private static List<LivingEntity> sleepers(
      final ServerWorld world, final ServerPlayerEntity maker, final int reach) {
    return world.getEntitiesByClass(
        LivingEntity.class,
        new Box(maker.getBlockPos()).expand(reach),
        sleeper ->
            sleeper != maker
                && sleeper.isAlive()
                && sleeper.isSleeping()
                && Vampires.isVampire(sleeper)
                && CoffinSleepers.inCoffin(sleeper)
                && Noise.carries(sleeper.squaredDistanceTo(maker), reach));
  }
}
