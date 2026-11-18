package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.config.MobSettings;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.household.HouseholdPlanner;
import com.grahambartley.nightborn.server.ServerConfigService;

public final class MobLevitation {

  static final int TICKS_PER_SECOND = 20;

  private MobLevitation() {}

  public static boolean wants(
      final boolean levitating,
      final boolean fighting,
      final boolean settled,
      final float roll,
      final MobSettings settings) {
    if (fighting && settings.vampireLevitateSeconds() > 0) {
      return true;
    }
    if (settled) {
      return false;
    }
    return levitating
        ? roll >= chancePerTick(settings.vampireLandSeconds())
        : roll < chancePerTick(settings.vampireLevitateSeconds());
  }

  public static void drift(final VampireEntity vampire) {
    final boolean levitating = Levitation.isLevitating(vampire);
    if (wants(
            levitating,
            vampire.getTarget() != null,
            HouseholdPlanner.settled(vampire),
            vampire.getRandom().nextFloat(),
            ServerConfigService.current().mobs())
        == levitating) {
      return;
    }
    if (levitating) {
      Levitation.setDown(vampire);
    } else {
      Levitation.start(vampire);
    }
  }

  static float chancePerTick(final int seconds) {
    return seconds <= 0 ? 0.0F : 1.0F / (seconds * TICKS_PER_SECOND);
  }
}
