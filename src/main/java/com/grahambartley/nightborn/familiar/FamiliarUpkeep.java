package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.coffin.AssignedCoffin;
import com.grahambartley.nightborn.config.FamiliarSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class FamiliarUpkeep {

  private FamiliarUpkeep() {}

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
    passDays(
        familiar, registry, FamiliarClock.today(world), ServerConfigService.current().familiars());
  }

  public static void passDays(
      final HumanFamiliarEntity familiar,
      final FamiliarRegistry registry,
      final long today,
      final FamiliarSettings settings) {
    familiar.lastDay(FamiliarDay.catchUpFrom(familiar.lastDay(), today));
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
    if (player != null) {
      AssignedCoffin.in(player, world).ifPresent(familiar::home);
    }
  }
}
