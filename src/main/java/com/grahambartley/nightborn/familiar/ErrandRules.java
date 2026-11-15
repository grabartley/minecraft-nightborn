package com.grahambartley.nightborn.familiar;

public final class ErrandRules {

  private ErrandRules() {}

  public static boolean inReach(
      final boolean fetchHere,
      final boolean deliverHere,
      final double fetchDistanceSquared,
      final double deliverDistanceSquared,
      final int range) {
    final double reach = (double) range * range;
    return fetchHere
        && deliverHere
        && fetchDistanceSquared <= reach
        && deliverDistanceSquared <= reach;
  }
}
