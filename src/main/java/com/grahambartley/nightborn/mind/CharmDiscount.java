package com.grahambartley.nightborn.mind;

public final class CharmDiscount {

  static final double BASE = 0.3;
  static final double PER_LEVEL = 0.0625;

  private CharmDiscount() {}

  public static int cut(final int price, final int charm) {
    if (charm <= 0 || price <= 0) {
      return 0;
    }
    return Math.max((int) Math.floor((BASE + PER_LEVEL * (charm - 1)) * price), 1);
  }

  public static boolean applies(
      final boolean charmedByCustomer, final int charm, final boolean hero) {
    return charmedByCustomer && charm > 0 && !hero;
  }
}
