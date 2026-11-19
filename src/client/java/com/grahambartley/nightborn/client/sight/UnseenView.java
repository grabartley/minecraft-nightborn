package com.grahambartley.nightborn.client.sight;

public final class UnseenView {

  private UnseenView() {}

  public static boolean skips(
      final boolean bodyHidden, final boolean revealed, final boolean outlinable) {
    return bodyHidden && !(revealed && outlinable);
  }

  public static boolean bodyHidden(final boolean gone, final boolean sheltered) {
    return gone || sheltered;
  }
}
