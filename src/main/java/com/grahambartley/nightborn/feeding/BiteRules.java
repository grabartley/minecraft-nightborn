package com.grahambartley.nightborn.feeding;

public final class BiteRules {

  public static final int PER_THOUSAND = 1000;
  static final int PERCENT = 100;

  private BiteRules() {}

  public enum Decision {
    BITE,
    FINISH,
    NOT_A_VAMPIRE,
    NOT_SNEAKING_EMPTY_HANDED,
    HELPLESS,
    NO_WARM_BLOOD,
    WARDED,
    PVP_OFF;

    public boolean bites() {
      return this == BITE;
    }

    public boolean finishes() {
      return this == FINISH;
    }

    public boolean claimsTheInteraction() {
      return this == BITE
          || this == FINISH
          || this == PVP_OFF
          || this == HELPLESS
          || this == WARDED;
    }
  }

  public record Biter(boolean vampire, boolean sneaking, boolean emptyHand, boolean downed) {}

  public record Target(
      boolean warmBlood,
      boolean vampire,
      boolean player,
      boolean alive,
      boolean warded,
      boolean finishable) {}

  public static boolean drinkable(final boolean warmBlood, final boolean vampire) {
    return warmBlood && !vampire;
  }

  public static boolean finishable(
      final float health, final float maxHealth, final int thresholdPercent) {
    return maxHealth > 0.0F && health > 0.0F && health < maxHealth * thresholdPercent / PERCENT;
  }

  public static boolean startsSickness(
      final boolean victimIsHumanPlayer,
      final boolean turningBite,
      final boolean playersCanTurnPlayers,
      final boolean firstRoll,
      final int roll,
      final int chancePerThousand) {
    return victimIsHumanPlayer
        && turningBite
        && playersCanTurnPlayers
        && firstRoll
        && roll < chancePerThousand;
  }

  public static int perThousand(final int percent) {
    return percent * PER_THOUSAND / PERCENT;
  }

  public static int turningCost(final int fullBar, final int percent) {
    return (int) Math.ceil(Math.max(0, fullBar) * Math.max(0, percent) / (double) PERCENT);
  }

  public static Decision decide(final Biter biter, final Target target, final boolean pvp) {
    if (!biter.vampire()) {
      return Decision.NOT_A_VAMPIRE;
    }
    if (!biter.emptyHand() || !biter.sneaking() && !target.finishable()) {
      return Decision.NOT_SNEAKING_EMPTY_HANDED;
    }
    if (!target.alive() || !drinkable(target.warmBlood(), target.vampire())) {
      return Decision.NO_WARM_BLOOD;
    }
    if (biter.downed()) {
      return Decision.HELPLESS;
    }
    if (target.warded()) {
      return Decision.WARDED;
    }
    if (target.player() && !pvp) {
      return Decision.PVP_OFF;
    }
    return biter.sneaking() ? Decision.BITE : Decision.FINISH;
  }
}
