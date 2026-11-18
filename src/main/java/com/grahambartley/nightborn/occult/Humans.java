package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public final class Humans {

  private Humans() {}

  public static boolean human(final Optional<BloodQuality> blood, final boolean vampire) {
    return !vampire && blood.filter(quality -> quality != BloodQuality.ANIMAL).isPresent();
  }

  public static boolean human(final LivingEntity entity) {
    return human(BloodQuality.of(entity.getType()), Vampires.isVampire(entity));
  }
}
