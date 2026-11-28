package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.mind.MindAttributes;
import com.grahambartley.nightborn.occult.OccultAttributes;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;

public final class VampireMobAttributes {

  static final double MAX_HEALTH = 30.0;
  static final double MOVEMENT_SPEED = 0.3;
  static final double ATTACK_DAMAGE = 3.0;
  static final double FOLLOW_RANGE = 32.0;

  private VampireMobAttributes() {}

  public static DefaultAttributeContainer.Builder create() {
    return WornAttributes.addTo(
        VampireAttributes.addTo(
            OccultAttributes.addTo(
                MindAttributes.addTo(
                    HostileEntity.createHostileAttributes()
                        .add(EntityAttributes.GENERIC_MAX_HEALTH, MAX_HEALTH)
                        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVEMENT_SPEED)
                        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, ATTACK_DAMAGE)
                        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, FOLLOW_RANGE)))));
  }
}
