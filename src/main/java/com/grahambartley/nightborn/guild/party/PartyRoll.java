package com.grahambartley.nightborn.guild.party;

import java.util.function.IntUnaryOperator;

public final class PartyRoll {

  public static final int PERCENT = 100;

  private PartyRoll() {}

  public static boolean succeeds(final IntUnaryOperator nextInt, final int chance) {
    return nextInt.applyAsInt(PERCENT) < chance;
  }

  public static int size(final IntUnaryOperator nextInt, final int min, final int max) {
    return min + nextInt.applyAsInt(Math.max(1, max - min + 1));
  }

  public static int nights(final IntUnaryOperator nextInt, final int extraChance, final int max) {
    int nights = 1;
    while (nights < max && succeeds(nextInt, extraChance)) {
      nights++;
    }
    return nights;
  }

  public static int sleepers(final int members, final int beds, final int share) {
    final int wanted = members * share / PERCENT;
    return Math.max(0, Math.min(wanted, Math.min(beds, members - 1)));
  }
}
