package com.grahambartley.nightborn.guild;

public final class MasterDuty {

  private MasterDuty() {}

  public static boolean breaksOath(final boolean swearing, final boolean fighting) {
    return swearing && fighting;
  }

  public static boolean closesCounter(final boolean vampireCustomer, final boolean fighting) {
    return vampireCustomer || fighting;
  }
}
