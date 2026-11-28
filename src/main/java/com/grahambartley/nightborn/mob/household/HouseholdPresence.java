package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.combat.PresenceAura;
import com.grahambartley.nightborn.mob.home.Housemates;
import net.minecraft.entity.mob.MobEntity;

public final class HouseholdPresence {

  private HouseholdPresence() {}

  public static void lead(final VampireEntity head) {
    head.stage()
        .combat()
        .presence()
        .ifPresent(
            presence -> PresenceAura.spread(head, presence, other -> housemate(head, other)));
  }

  private static boolean housemate(final VampireEntity head, final MobEntity other) {
    return other instanceof VampireEntity mate
        && Housemates.share(head.homeLife().home(), mate.homeLife().home());
  }
}
