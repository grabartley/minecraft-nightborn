package com.grahambartley.nightborn.ward;

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
    if (vampire instanceof ServerPlayerEntity player) {
      PlayerCues.send(player, PlayerCue.FLINCH);
    } else if (vampire instanceof PowerShow mob) {
      mob.showPower(VampirePose.FLINCH.animation());
    }
  }
}
