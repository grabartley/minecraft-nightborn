package com.grahambartley.nightborn.guild;

public final class CounterClosing {

  private CounterClosing() {}

  public static boolean closes(final boolean vampireCustomer, final boolean fighting) {
    return vampireCustomer || fighting;
  }
}
