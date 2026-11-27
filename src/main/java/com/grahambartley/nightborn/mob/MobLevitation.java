package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.config.MobSettings;
import com.grahambartley.nightborn.levitation.FloatRoom;
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
      final boolean roomToFloat,
      final float roll,
      final MobSettings settings) {
    if (!roomToFloat) {
      return false;
    }
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
    final boolean roomToFloat = !Unsticking.holdsDown(vampire) && FloatRoom.ahead(vampire);
    if (wants(
            levitating,
            vampire.getTarget() != null,
            HouseholdPlanner.settled(vampire),
            roomToFloat,
            vampire.getRandom().nextFloat(),
            ServerConfigService.current().mobs())
        == levitating) {
      return;
    }
    if (levitating && !roomToFloat) {
      Levitation.sink(vampire);
    } else if (levitating) {
      Levitation.setDown(vampire);
    } else {
      Levitation.start(vampire);
    }
  }

  static float chancePerTick(final int seconds) {
    return seconds <= 0 ? 0.0F : 1.0F / (seconds * TICKS_PER_SECOND);
  }
}
