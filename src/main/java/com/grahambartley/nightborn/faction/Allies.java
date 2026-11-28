package com.grahambartley.nightborn.faction;

import com.grahambartley.nightborn.guild.GuildFighter;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;

public final class Allies {

  private Allies() {}

  public static boolean allied(final LivingEntity viewer, final LivingEntity other) {
    return Sides.allied(
        viewer == other,
        Sides.sameSide(
            Hunters.isHunter(viewer),
            Vampires.isVampire(viewer),
            Hunters.isHunter(other),
            Vampires.isVampire(other)),
        viewer.isTeammate(other),
        fighting(viewer, other) || fighting(other, viewer));
  }

  public static boolean opposed(final LivingEntity viewer, final LivingEntity other) {
    return Sides.opposed(
        Hunters.isHunter(viewer),
        Vampires.isVampire(viewer),
        Hunters.isHunter(other),
        Vampires.isVampire(other));
  }

  private static boolean fighting(final LivingEntity one, final LivingEntity other) {
    return Powers.foe(one) == other
        || other.getAttacker() == one
        || one instanceof GuildFighter fighter && fighter.foeOf(other);
  }
}
