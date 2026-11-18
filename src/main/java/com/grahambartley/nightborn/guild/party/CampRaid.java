package com.grahambartley.nightborn.guild.party;

import java.util.List;
import java.util.stream.IntStream;

public final class CampRaid {

  public static final double WAKE_RANGE = 24.0;

  private CampRaid() {}

  public static boolean due(
      final boolean night, final boolean pitched, final boolean rolledTonight) {
    return night && pitched && !rolledTonight;
  }

  public static List<Integer> wakers(final List<Double> sleeperDistances) {
    return IntStream.range(0, sleeperDistances.size())
        .filter(i -> sleeperDistances.get(i) <= WAKE_RANGE)
        .boxed()
        .toList();
  }

  public static boolean over(final int raidersNear) {
    return raidersNear == 0;
  }
}
