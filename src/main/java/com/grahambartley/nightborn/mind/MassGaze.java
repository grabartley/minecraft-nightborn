package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.faction.Allies;
import com.grahambartley.nightborn.skill.MesmerisePower;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

public final class MassGaze {

  private MassGaze() {}

  public static boolean gazes(
      final boolean mob,
      final boolean player,
      final boolean vampire,
      final boolean seen,
      final boolean allied) {
    return seen && !allied && (mob || player && !vampire);
  }

  public static int cast(final LivingEntity caster, final MassGazePower power) {
    final MesmerisePower mesmerise =
        new MesmerisePower(
            power.radius(),
            1,
            power.durationTicks(),
            power.playerDurationTicks(),
            power.playerSlowness());
    final double radius = power.radius();
    int held = 0;
    for (final LivingEntity living :
        caster
            .getWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(caster.getBlockPos()).expand(radius),
                candidate ->
                    candidate.squaredDistanceTo(caster) <= radius * radius
                        && PowerReach.touches(caster, candidate)
                        && gazes(
                            candidate instanceof MobEntity,
                            candidate instanceof PlayerEntity,
                            Vampires.isVampire(candidate),
                            caster.canSee(candidate),
                            Allies.spared(caster, candidate)))) {
      if (Mesmerise.quietly(caster, living, mesmerise) == MesmeriseRules.Outcome.MESMERISED) {
        held++;
      }
    }
    if (caster instanceof PlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.mass_gaze.held", held), true);
    }
    return held;
  }
}
