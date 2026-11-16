package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.Housemates;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public final class HouseholdBonds {

  private HouseholdBonds() {}

  public static void alert(final VampireEntity hurt, final LivingEntity attacker, final int range) {
    final boolean attackerIsVampire = Vampires.isVampire(attacker);
    for (final VampireEntity mate : near(hurt, range, attacker)) {
      if (!mate.isSleeping()
          && Housemates.alerted(
              Housemates.share(hurt.homeLife().home(), mate.homeLife().home()),
              attackerIsVampire,
              mate.squaredDistanceTo(hurt),
              range)) {
        mate.setAttacker(attacker);
      }
    }
  }

  public static void mourn(final ServerWorld world, final VampireEntity dead, final int range) {
    for (final VampireEntity mate : near(dead, range, null)) {
      if (Housemates.alerted(
          Housemates.share(dead.homeLife().home(), mate.homeLife().home()),
          false,
          mate.squaredDistanceTo(dead),
          range)) {
        mate.mourn(world);
      }
    }
  }

  private static List<VampireEntity> near(
      final VampireEntity center, final int range, @Nullable final LivingEntity besides) {
    return center
        .getWorld()
        .getEntitiesByClass(
            VampireEntity.class,
            center.getBoundingBox().expand(range),
            mate -> mate != center && mate != besides && mate.isAlive());
  }
}
