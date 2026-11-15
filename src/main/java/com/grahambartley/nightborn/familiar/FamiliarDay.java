package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.config.FamiliarSettings;

public final class FamiliarDay {

  private FamiliarDay() {}

  public record Outcome(int loyalty, boolean betrays) {}

  public static Outcome pass(
      final int loyalty, final boolean paid, final FamiliarSettings settings, final int roll) {
    final int after =
        Loyalty.change(loyalty, paid ? settings.paidDayLoyalty() : -settings.unpaidDayLoyalty());
    final boolean betrays = after < settings.betrayLoyalty() && roll < settings.betrayChance();
    return new Outcome(after, betrays);
  }
}
