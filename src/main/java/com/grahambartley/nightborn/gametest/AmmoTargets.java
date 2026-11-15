package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

final class AmmoTargets {

  static final float TOUGH = 100.0F;

  private AmmoTargets() {}

  static VampireEntity vampire(final TestContext context, final BlockPos relative) {
    return tough(context.spawnEntity(NightbornMobs.VAMPIRE, relative));
  }

  static <T extends MobEntity> T tough(final T mob) {
    mob.setAiDisabled(true);
    mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(TOUGH);
    mob.setHealth(TOUGH);
    return mob;
  }
}
