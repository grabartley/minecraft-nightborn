package com.grahambartley.nightborn.guild.veteran;

import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

public final class VeteranChoice {

  public record Situation(
      boolean sees,
      double distance,
      boolean airborne,
      boolean bat,
      int vampiresNear,
      int stillTicks,
      boolean allyMesmerised,
      double partyHealth) {}

  public static final double TELEPORT = 3.0;
  public static final double STILL_MOVE = 0.05;

  private VeteranChoice() {}

  public static boolean fits(final VeteranAttack attack, final Situation situation) {
    final VeteranAttack.Trigger trigger = attack.trigger();
    final boolean inReach = situation.sees() && situation.distance() <= attack.reach();
    return switch (attack.kind()) {
      case SILVER_CHAIN ->
          inReach
              && (situation.distance() > trigger.meleeReach()
                  || situation.airborne()
                  || situation.bat());
      case BOLT_VOLLEY -> inReach && situation.distance() >= attack.reachMin();
      case HOLY_WATER_ARC ->
          inReach
              && (trigger.crowd() > 0 && situation.vampiresNear() >= trigger.crowd()
                  || trigger.stillTicks() > 0 && situation.stillTicks() >= trigger.stillTicks());
      case RALLYING_CRY ->
          situation.allyMesmerised() || situation.partyHealth() <= trigger.partyHealth();
    };
  }

  public static Optional<Identifier> pick(
      final Map<Identifier, VeteranAttack> attacks,
      final Map<Identifier, Long> readyAt,
      final long now,
      final Situation situation) {
    return attacks.entrySet().stream()
        .filter(entry -> now >= readyAt.getOrDefault(entry.getKey(), Long.MIN_VALUE))
        .filter(entry -> fits(entry.getValue(), situation))
        .map(Map.Entry::getKey)
        .findFirst();
  }

  public static boolean dodged(final double movedInOneTick) {
    return movedInOneTick > TELEPORT;
  }

  public static int stillTicks(final int before, final double movedInOneTick) {
    return movedInOneTick < STILL_MOVE ? before + 1 : 0;
  }

  public static boolean chainLands(
      final boolean dodged,
      final boolean alive,
      final boolean vanished,
      final boolean sees,
      final double distance,
      final double reach) {
    return !dodged && alive && !vanished && sees && distance <= reach;
  }

  public static double partyHealth(
      final double standingHealth,
      final double standingMax,
      final int unseen,
      final int fallen,
      final double hunterMax) {
    final double health = standingHealth + unseen * hunterMax;
    final double max = standingMax + (unseen + fallen) * hunterMax;
    return max <= 0.0 ? 1.0 : health / max;
  }
}
