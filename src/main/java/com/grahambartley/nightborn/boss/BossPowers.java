package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillPower;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class BossPowers {

  private BossPowers() {}

  public static int fed(final int blood, final int cost) {
    return Math.max(blood, cost);
  }

  public static PowerUse.Verdict cast(final LivingEntity boss, final Identifier power) {
    final int cost = Powers.power(boss, power).map(SkillPower::blood).orElse(0);
    BloodAttachment.set(boss, fed(BloodAttachment.get(boss), cost));
    PowerCooldownAttachment.set(
        boss, PowerCooldownAttachment.get(boss).start(power, boss.getWorld().getTime(), 0));
    return Powers.cast(boss, power);
  }
}
