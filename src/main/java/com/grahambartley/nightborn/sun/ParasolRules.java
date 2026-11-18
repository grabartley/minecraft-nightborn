package com.grahambartley.nightborn.sun;

public final class ParasolRules {

  private ParasolRules() {}

  public static boolean closes(
      final boolean sprinting, final boolean levitating, final boolean batForm) {
    return sprinting || levitating || batForm;
  }

  public static boolean shades(final boolean open, final boolean closing) {
    return open && !closing;
  }

  public static boolean blow(
      final boolean struckBySomeone, final float damageTaken, final boolean blocked) {
    return struckBySomeone && damageTaken > 0.0F && !blocked;
  }
}
