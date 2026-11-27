package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.clothing.Noticing;
import com.grahambartley.nightborn.clothing.WornAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;

public class HunterTargetGoal extends ActiveTargetGoal<LivingEntity> {

  static final double NEAR = 8.0;

  private boolean nearOnly;

  public <T extends MobEntity & GuildFighter> HunterTargetGoal(final T hunter) {
    super(hunter, LivingEntity.class, true, GuildFoes::hunts);
  }

  @Override
  public boolean canStart() {
    final boolean chanceComesUp = mob.getRandom().nextInt(reciprocalChance) == 0;
    nearOnly = !chanceComesUp;
    findClosestTarget();
    nearOnly = false;
    return targetEntity != null
        && HunterTargets.noticesNow(mob.squaredDistanceTo(targetEntity), NEAR, chanceComesUp);
  }

  public static double reach(final double followRange, final double seen) {
    return followRange * seen;
  }

  @Override
  protected Box getSearchBox(final double distance) {
    return nearOnly
        ? super.getSearchBox(NEAR)
        : super.getSearchBox(distance * WornAttributes.MOST_SIGHT);
  }

  @Override
  protected double getFollowRange() {
    final LivingEntity target = this.mob.getTarget();
    return target == null
        ? super.getFollowRange()
        : reach(super.getFollowRange(), Noticing.byHunter(target));
  }
}
