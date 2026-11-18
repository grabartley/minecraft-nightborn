package com.grahambartley.nightborn.raid;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.HouseholdRoll;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;

public final class HouseWatch {

  private HouseWatch() {}

  public static boolean keepsWatch(final VampireEntity vampire) {
    if (!(vampire.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    return vampire
        .homeLife()
        .home()
        .filter(
            home -> {
              final long day = NightRoutine.night(world.getTimeOfDay());
              return DayWatch.watchDay(
                      day, home.asLong(), ServerConfigService.current().raids().watchDays())
                  && DayWatch.watcher(
                          HouseholdRoll.get(world.getServer())
                              .record(GlobalPos.create(world.getRegistryKey(), home))
                              .living(),
                          day)
                      .filter(vampire.getUuid()::equals)
                      .isPresent();
            })
        .isPresent();
  }

  public static boolean onWatch(final VampireEntity vampire) {
    return vampire.homeLife().bedtime(vampire) && keepsWatch(vampire);
  }
}
