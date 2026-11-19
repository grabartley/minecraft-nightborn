package com.grahambartley.nightborn.client.sight;

public final class UnseenView {

  private UnseenView() {}

  public static boolean hides(final boolean gone, final boolean sheltered, final boolean revealed) {
    return bodyHidden(gone, sheltered) && !revealed;
  }

  public static boolean bodyHidden(final boolean gone, final boolean sheltered) {
    return gone || sheltered;
  }
}
