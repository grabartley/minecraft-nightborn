package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.clothing.Noticing;
import com.grahambartley.nightborn.clothing.WornAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;

public class HunterTargetGoal extends ActiveTargetGoal<LivingEntity> {

  public HunterTargetGoal(final MobEntity hunter) {
    super(hunter, LivingEntity.class, true, HunterEntity::hunts);
  }

  public static double reach(final double followRange, final double seen) {
    return followRange * seen;
  }

  @Override
  protected Box getSearchBox(final double distance) {
    return super.getSearchBox(distance * WornAttributes.MOST_SIGHT);
  }

  @Override
  protected double getFollowRange() {
    final LivingEntity target = this.mob.getTarget();
    return target == null
        ? super.getFollowRange()
        : reach(super.getFollowRange(), Noticing.byHunter(target));
  }
}
