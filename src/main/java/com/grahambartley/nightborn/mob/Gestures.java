package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.network.PlayerCue;
import com.grahambartley.nightborn.network.PlayerCues;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Gestures {

  private Gestures() {}

  public static void show(final LivingEntity vampire, final VampirePose gesture) {
    if (vampire instanceof ServerPlayerEntity player) {
      if (Vampires.isVampire(player)) {
        PlayerCue.of(gesture).ifPresent(cue -> PlayerCues.send(player, cue));
      }
    } else if (vampire instanceof PowerShow mob) {
      mob.showPower(gesture.animation());
    }
  }
}
