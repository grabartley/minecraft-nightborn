package com.grahambartley.nightborn.guild;

public final class MasterDuty {

  private MasterDuty() {}

  public static boolean watches(final boolean trading, final boolean swearing) {
    return !trading && !swearing;
  }

  public static boolean closesCounter(final boolean vampireCustomer, final boolean fighting) {
    return vampireCustomer || fighting;
  }
}
