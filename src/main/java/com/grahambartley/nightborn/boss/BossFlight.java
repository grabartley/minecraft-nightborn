package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.Optional;
import net.minecraft.entity.mob.MobEntity;

public final class BossFlight {

  private boolean flying;

  public boolean flying() {
    return flying;
  }

  public static boolean flies(final boolean wantsTheAir, final boolean weakened) {
    return wantsTheAir && !weakened;
  }

  public static boolean grounds(final boolean airborne, final boolean aloft, final boolean bat) {
    return !airborne && aloft && !bat;
  }

  public <T extends MobEntity & PowerShow> void tick(
      final T boss,
      final FightClips clips,
      final Optional<PhaseFlight> flight,
      final Arena arena,
      final boolean fighting,
      final boolean held,
      final boolean diving,
      final long sinceLanding,
      final long now) {
    final boolean airborne =
        flies(
            flight.map(f -> f.airborne(fighting, held, diving, sinceLanding)).orElse(false),
            Weakened.sources(boss).level(false) > 0);
    if (airborne) {
      boss.setNoGravity(true);
      boss.setVelocity(flight.get().steer(boss.getPos(), flight.get().spot(arena, now)));
      boss.velocityModified = true;
      boss.fallDistance = 0.0F;
    } else if (grounds(
        false, boss.hasNoGravity() || Levitation.isLevitating(boss), BatForm.isBat(boss))) {
      boss.setNoGravity(false);
      Levitation.land(boss);
    }
    final String edge = FightClips.flightEdge(flying, airborne, clips);
    if (!edge.isEmpty()) {
      boss.showPower(edge);
    }
    flying = airborne;
  }
}
