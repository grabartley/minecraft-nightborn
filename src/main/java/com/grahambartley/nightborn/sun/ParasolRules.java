package com.grahambartley.nightborn.sun;

public final class ParasolRules {

  public enum Use {
    OPEN,
    CLOSE,
    REFUSE
  }

  private ParasolRules() {}

  public static Use use(final boolean open, final boolean closing) {
    if (open) {
      return Use.CLOSE;
    }
    return closing ? Use.REFUSE : Use.OPEN;
  }

  public static boolean closes(
      final boolean sprinting,
      final boolean levitating,
      final boolean batForm,
      final boolean lyingDown) {
    return sprinting || levitating || batForm || lyingDown;
  }

  public static boolean shades(final boolean open, final boolean closing) {
    return open && !closing;
  }

  public static boolean blow(
      final boolean struckBySomeone, final float damageTaken, final boolean blocked) {
    return struckBySomeone && damageTaken > 0.0F && !blocked;
  }
}
