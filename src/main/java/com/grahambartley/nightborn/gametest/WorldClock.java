package com.grahambartley.nightborn.gametest;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

final class WorldClock {

  static final int NOON = 6000;
  static final int MIDNIGHT = 18000;

  private static long timeBefore;
  private static boolean cycleBefore;

  private WorldClock() {}

  static void freeze(final ServerWorld world, final int time) {
    set(world, time, false);
  }

  static void runFrom(final ServerWorld world, final int time) {
    set(world, time, true);
  }

  private static void set(final ServerWorld world, final int time, final boolean cycle) {
    timeBefore = world.getTimeOfDay();
    cycleBefore = world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE);
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(cycle, world.getServer());
    world.setTimeOfDay(time);
    world.setWeather(12000, 0, false, false);
  }

  static void release(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(cycleBefore, world.getServer());
    world.setTimeOfDay(timeBefore);
  }
}
