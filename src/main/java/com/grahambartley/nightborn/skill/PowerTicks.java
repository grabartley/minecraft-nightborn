package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.mind.BloodTithe;
import com.grahambartley.nightborn.occult.MistForm;
import net.minecraft.entity.LivingEntity;

public final class PowerTicks {

  private PowerTicks() {}

  public static void tick(final LivingEntity entity) {
    Holds.tick(entity);
    Boosts.tick(entity);
    MistForm.tick(entity);
    BloodTithe.tick(entity);
  }
}
