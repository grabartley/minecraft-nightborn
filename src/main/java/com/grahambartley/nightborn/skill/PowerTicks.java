package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faith.Kindling;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.mind.BloodTithe;
import com.grahambartley.nightborn.occult.MistForm;
import com.grahambartley.nightborn.slayer.Dashes;
import com.grahambartley.nightborn.slayer.Guards;
import com.grahambartley.nightborn.trap.Grounding;
import net.minecraft.entity.LivingEntity;

public final class PowerTicks {

  private PowerTicks() {}

  public static void tick(final LivingEntity entity) {
    Holds.tick(entity);
    Boosts.tick(entity);
    MistForm.tick(entity);
    Grounding.tick(entity);
    BloodTithe.tick(entity);
    Kindling.tick(entity);
    Guards.tick(entity);
    Dashes.tick(entity);
  }
}
