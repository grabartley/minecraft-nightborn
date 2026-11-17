package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.levitation.Levitation;
import java.util.Optional;
import net.minecraft.entity.mob.MobEntity;

public final class BossFlight {

  private boolean flying;

  public boolean flying() {
    return flying;
  }

  public static boolean grounds(
      final boolean airborne, final boolean weightless, final boolean bat) {
    return !airborne && weightless && !bat;
  }

  public void tick(
      final MobEntity boss,
      final Optional<PhaseFlight> flight,
      final Arena arena,
      final boolean fighting,
      final boolean held,
      final boolean diving,
      final long sinceLanding,
      final long now) {
    final boolean airborne =
        flight.map(f -> f.airborne(fighting, held, diving, sinceLanding)).orElse(false)
            && (Levitation.isLevitating(boss) || Levitation.start(boss));
    if (airborne) {
      boss.setNoGravity(true);
      boss.setVelocity(flight.get().steer(boss.getPos(), flight.get().spot(arena, now)));
      boss.velocityModified = true;
      boss.fallDistance = 0.0F;
    } else if (grounds(false, boss.hasNoGravity(), BatForm.isBat(boss))) {
      boss.setNoGravity(false);
      Levitation.land(boss);
    }
    flying = airborne;
  }
}
