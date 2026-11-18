package com.grahambartley.nightborn.ward;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.mind.Glamour;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.network.PlayerCue;
import com.grahambartley.nightborn.network.PlayerCues;
import com.grahambartley.nightborn.skill.PowerShow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Flinch {

  private Flinch() {}

  public static boolean starts(final boolean wasInReach, final boolean inReach) {
    return !wasInReach && inReach;
  }

  public static boolean inReach(final int strings, final int crucifix, final int shape) {
    return strings > 0 || crucifix > 0 || shape > 0;
  }

  public static void show(final LivingEntity vampire) {
    if (Glamour.on(vampire)) {
      return;
    }
    final boolean small = small(GiftAttributes.value(vampire, GiftAttributes.SMALL_FLINCH));
    if (vampire instanceof ServerPlayerEntity player) {
      PlayerCues.send(player, small ? PlayerCue.SMALL_FLINCH : PlayerCue.FLINCH);
    } else if (vampire instanceof PowerShow mob) {
      mob.showPower((small ? VampirePose.SMALL_FLINCH : VampirePose.FLINCH).animation());
    }
  }

  public static boolean small(final double smallFlinch) {
    return smallFlinch > 0.0;
  }
}
