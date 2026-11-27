package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.mob.combat.PowerCasting;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

final class GuildFighting<T extends MobEntity & GuildFighter & PowerShow> {

  static final int BLOOD_FLOOR = 0;

  private final T fighter;
  private final HunterTactics<T> tactics;
  private final PowerCasting casting = PowerCasting.underTell();

  GuildFighting(final T fighter) {
    this.fighter = fighter;
    this.tactics = new HunterTactics<>(fighter);
  }

  void tick(@Nullable final HunterKit kit, final int thinkTicks) {
    if (kit != null && !casting.telling()) {
      tactics.tick(kit);
    }
    casting.tick(fighter, thinkTicks, BLOOD_FLOOR);
  }

  boolean strike(final boolean struck) {
    if (struck) {
      fighter.speak(HunterVoice.STRIKE);
    }
    return struck;
  }

  void killed(final LivingEntity other) {
    if (Vampires.isVampire(other)) {
      fighter.speak(HunterVoice.CHEER);
    }
  }

  void shoot() {
    tactics.shoot();
  }

  boolean telling() {
    return casting.telling();
  }
}
