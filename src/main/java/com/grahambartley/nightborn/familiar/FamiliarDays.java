package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.config.FamiliarSettings;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FamiliarDays {

  static final int MOST_DAYS_AT_ONCE = 7;

  private FamiliarDays() {}

  public static long today(final World world) {
    return Math.floorDiv(world.getTimeOfDay(), NightTime.DAY_LENGTH);
  }

  public static boolean isWorkingHours(final World world) {
    return !NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay());
  }

  public static void check(final HumanFamiliarEntity familiar) {
    if (!(familiar.getWorld() instanceof ServerWorld world) || familiar.owner().isEmpty()) {
      return;
    }
    final UUID owner = familiar.owner().get();
    final FamiliarRegistry registry = FamiliarRegistry.of(world.getServer());
    if (!familiar.turned() && !registry.keeps(owner, familiar.getUuid())) {
      FamiliarRelease.release(familiar);
      return;
    }
    followCoffin(familiar, world, owner);
    passDays(familiar, registry, today(world), ServerConfigService.current().familiars());
  }

  public static void passDays(
      final HumanFamiliarEntity familiar,
      final FamiliarRegistry registry,
      final long today,
      final FamiliarSettings settings) {
    if (familiar.lastDay() < 0L || today - familiar.lastDay() > MOST_DAYS_AT_ONCE) {
      familiar.lastDay(Math.max(familiar.lastDay(), today - MOST_DAYS_AT_ONCE));
    }
    while (familiar.lastDay() < today) {
      final long ended = familiar.lastDay();
      final FamiliarDay.Outcome outcome =
          FamiliarDay.pass(
              familiar.loyalty(),
              Wages.paidFor(familiar.paidThroughDay(), ended),
              settings,
              familiar.getRandom().nextInt(100));
      familiar.loyalty(outcome.loyalty());
      familiar.lastDay(ended + 1);
      if (outcome.betrays() && !familiar.turned()) {
        familiar.turn();
        familiar.owner().ifPresent(owner -> registry.free(owner, familiar.getUuid()));
      }
    }
  }

  private static void followCoffin(
      final HumanFamiliarEntity familiar, final ServerWorld world, final UUID owner) {
    final ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(owner);
    if (player == null || player.getSpawnPointDimension() != world.getRegistryKey()) {
      return;
    }
    final BlockPos coffin = player.getSpawnPointPosition();
    if (coffin != null && world.getBlockState(coffin).getBlock() instanceof CoffinBlock) {
      familiar.home(coffin);
    }
  }
}
