package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.skill.PowerShow;
import net.minecraft.entity.mob.MobEntity;

public final class Fighting {

  private final PowerCasting casting = new PowerCasting();
  private final CombatMoves moves = new CombatMoves();

  public static boolean fights(
      final boolean hasTarget,
      final boolean downed,
      final boolean asleep,
      final boolean retreating,
      final boolean weakened) {
    return hasTarget && !downed && !asleep && !retreating && !weakened;
  }

  public <T extends MobEntity & PowerShow> void tick(
      final T fighter,
      final boolean fighting,
      final StageCombat combat,
      final CombatSettings settings) {
    if (!fighting) {
      casting.cancel();
      moves.rest();
      return;
    }
    casting.tick(fighter, combat.thinkTicks(), combat.bloodFloor());
    if (!casting.telling()) {
      moves.tick(fighter, combat, settings);
    }
  }
}
