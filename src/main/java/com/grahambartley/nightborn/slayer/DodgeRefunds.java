package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerGifts;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPower;
import com.grahambartley.nightborn.skill.SkillPowerGifts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class DodgeRefunds {

  private DodgeRefunds() {}

  public static void refund(final LivingEntity hunter) {
    final long now = hunter.getWorld().getTime();
    for (final Identifier id : SkillAttachment.owned(hunter).ownedInOrder()) {
      final boolean dashes =
          Powers.power(hunter, id).flatMap(SkillPower::dash).isPresent()
              && PowerGifts.refundsOnDodge(SkillPowerGifts.forPower(hunter, id));
      if (dashes) {
        PowerCooldownAttachment.set(hunter, PowerCooldownAttachment.get(hunter).start(id, now, 0));
      }
    }
  }
}
