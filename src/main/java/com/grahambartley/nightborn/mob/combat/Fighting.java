package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.skill.PowerShow;
import java.util.function.Predicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

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
      final CombatSettings settings,
      final Predicate<Identifier> casts) {
    if (!fighting) {
      casting.cancel();
      moves.rest();
      return;
    }
    casting.tick(fighter, combat, casts);
    if (!casting.telling()) {
      moves.tick(fighter, combat, settings, casts);
    }
  }
}
