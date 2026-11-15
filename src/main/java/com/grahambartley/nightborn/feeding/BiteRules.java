package com.grahambartley.nightborn.feeding;

public final class BiteRules {

  private BiteRules() {}

  public enum Decision {
    BITE,
    NOT_A_VAMPIRE,
    NOT_SNEAKING_EMPTY_HANDED,
    HELPLESS,
    NO_WARM_BLOOD,
    WARDED,
    PVP_OFF;

    public boolean bites() {
      return this == BITE;
    }

    public boolean claimsTheInteraction() {
      return this == BITE || this == PVP_OFF || this == HELPLESS || this == WARDED;
    }
  }

  public record Biter(boolean vampire, boolean sneaking, boolean emptyHand, boolean downed) {}

  public record Target(
      boolean warmBlood, boolean vampire, boolean player, boolean alive, boolean warded) {}

  public static boolean drinkable(final boolean warmBlood, final boolean vampire) {
    return warmBlood && !vampire;
  }

  public static boolean startsSickness(
      final boolean victimIsHumanPlayer,
      final boolean playersCanTurnPlayers,
      final boolean firstRoll,
      final int roll,
      final int chancePercent) {
    return victimIsHumanPlayer && playersCanTurnPlayers && firstRoll && roll < chancePercent;
  }

  public static int resistedChance(final int chancePercent, final double biteResistance) {
    final double kept = 1.0 - Math.max(0.0, Math.min(1.0, biteResistance));
    return (int) Math.round(chancePercent * kept);
  }

  public static Decision decide(final Biter biter, final Target target, final boolean pvp) {
    if (!biter.vampire()) {
      return Decision.NOT_A_VAMPIRE;
    }
    if (!biter.sneaking() || !biter.emptyHand()) {
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
    return Decision.BITE;
  }
}
