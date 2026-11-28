package com.grahambartley.nightborn.guild;

public final class WaresWatch {

  static final double REACH = 4.0;

  private WaresWatch() {}

  public static boolean closes(
      final boolean hunterAlive,
      final boolean buyerThere,
      final boolean vampireBuyer,
      final boolean fighting,
      final double squaredDistance) {
    return !hunterAlive
        || !buyerThere
        || MasterDuty.closesCounter(vampireBuyer, fighting)
        || squaredDistance > REACH * REACH;
  }
}
