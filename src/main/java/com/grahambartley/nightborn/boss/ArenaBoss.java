package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.lord.BossPresence;
import com.grahambartley.nightborn.skill.PowerShow;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;

public interface ArenaBoss extends PowerShow {

  BossPresence presence();

  boolean attacksOnSight(LivingEntity target);

  default Set<UUID> sworn() {
    return Set.of();
  }
}
