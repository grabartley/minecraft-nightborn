package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.Housemates;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class Nemeses {

  private Nemeses() {}

  public static boolean isQuarry(final Entity nemesis, final LivingEntity target) {
    return quarry(nemesis).filter(target.getUuid()::equals).isPresent();
  }

  public static Optional<UUID> quarry(final Entity nemesis) {
    return NemesisAttachment.get(nemesis).map(NemesisMark::quarry);
  }

  public static void escaped(final LivingEntity vampire, final Optional<UUID> downedBy) {
    if (vampire instanceof MobEntity mob) {
      downedBy.ifPresent(human -> NemesisMaking.make(mob, human));
    }
  }

  public static void drewAway(
      final ServerWorld world,
      final VampireEntity guest,
      final Optional<BlockPos> formerHome,
      final UUID lord) {
    if (formerHome.isEmpty()) {
      return;
    }
    final int range = ServerConfigService.current().mobs().vampireAlertRange();
    world
        .getEntitiesByClass(
            VampireEntity.class,
            guest.getBoundingBox().expand(range),
            housemate ->
                housemate != guest
                    && housemate.isAlive()
                    && Housemates.share(housemate.home(), formerHome)
                    && housemate.householdTie().lord().isEmpty())
        .stream()
        .min(
            Comparator.comparing((VampireEntity housemate) -> !housemate.householdTie().head())
                .thenComparingDouble(housemate -> housemate.squaredDistanceTo(guest)))
        .ifPresent(housemate -> NemesisMaking.make(housemate, lord));
  }

  public static void forget(final ServerPlayerEntity quarry) {
    NemesisLedger.of(quarry.server)
        .forget(quarry.getUuid())
        .forEach(
            entry -> {
              final Entity mob = quarry.server.getOverworld().getEntity(entry.mob());
              if (mob != null) {
                NemesisAttachment.clear(mob);
              }
            });
  }
}
