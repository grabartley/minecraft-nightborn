package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import net.minecraft.entity.LivingEntity;

public final class MarkableVampires {

  private MarkableVampires() {}

  public static List<LivingEntity> near(final LivingEntity hunter, final float range) {
    return hunter
        .getWorld()
        .getEntitiesByClass(
            LivingEntity.class,
            hunter.getBoundingBox().expand(range),
            candidate ->
                candidate != hunter
                    && candidate.isAlive()
                    && !candidate.isSpectator()
                    && Vampires.isVampire(candidate)
                    && PowerReach.marks(candidate)
                    && candidate.squaredDistanceTo(hunter) <= (double) range * range);
  }
}
