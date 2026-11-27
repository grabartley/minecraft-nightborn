package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;

final class HunterCries {

  private HunterCries() {}

  static boolean strike(final GuildFighter fighter, final boolean struck) {
    if (struck) {
      fighter.speak(HunterVoice.STRIKE);
    }
    return struck;
  }

  static void killed(final GuildFighter fighter, final LivingEntity other) {
    if (Vampires.isVampire(other)) {
      fighter.speak(HunterVoice.CHEER);
    }
  }
}
