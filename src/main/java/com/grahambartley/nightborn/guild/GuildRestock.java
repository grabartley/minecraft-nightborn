package com.grahambartley.nightborn.guild;

public final class GuildRestock {

  public static final long DAY_TICKS = 24000L;

  private GuildRestock() {}

  public static long day(final long timeOfDay) {
    return Math.floorDiv(timeOfDay, DAY_TICKS);
  }

  public static boolean due(final long lastRestockDay, final long today) {
    return today != lastRestockDay;
  }
}
