package com.grahambartley.nightborn.familiar;

import java.util.OptionalLong;

public final class Wages {

  private Wages() {}

  public static OptionalLong pay(
      final long paidThroughDay, final long today, final int daysPerEmerald, final int daysAhead) {
    final long paidThrough = Math.max(paidThroughDay, today - 1) + daysPerEmerald;
    if (paidThroughDay >= today + daysAhead - 1) {
      return OptionalLong.empty();
    }
    return OptionalLong.of(Math.min(paidThrough, today + daysAhead - 1));
  }

  public static boolean paidFor(final long paidThroughDay, final long day) {
    return day <= paidThroughDay;
  }
}
