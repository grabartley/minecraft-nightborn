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
      boolean still,
      boolean allyMesmerised,
      double partyHealth) {}

  public static final double MELEE_REACH = 3.0;
  public static final double LOW_PARTY_HEALTH = 0.5;
  public static final int CROWD = 2;
  public static final double TELEPORT = 3.0;

  private VeteranChoice() {}

  public static boolean fits(final VeteranAttack attack, final Situation situation) {
    final boolean inReach = situation.sees() && situation.distance() <= attack.reach();
    return switch (attack.kind()) {
      case SILVER_CHAIN ->
          inReach
              && (situation.distance() > MELEE_REACH || situation.airborne() || situation.bat());
      case BOLT_VOLLEY -> inReach && situation.distance() >= attack.reachMin();
      case HOLY_WATER_ARC -> inReach && (situation.vampiresNear() >= CROWD || situation.still());
      case RALLYING_CRY ->
          situation.allyMesmerised() || situation.partyHealth() <= LOW_PARTY_HEALTH;
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

  public static double partyHealth(final double health, final double maxHealth) {
    return maxHealth <= 0.0 ? 1.0 : health / maxHealth;
  }
}
