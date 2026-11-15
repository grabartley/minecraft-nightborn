package com.grahambartley.nightborn.feeding;

public final class ResistChance {

  static final float REFERENCE_HEALTH = 20.0F;

  private ResistChance() {}

  public static float of(
      final float health, final float maxHealth, final int resistPercent, final boolean helpless) {
    if (helpless || maxHealth <= 0.0F || health <= 0.0F) {
      return 0.0F;
    }
    final float strength = maxHealth / REFERENCE_HEALTH;
    final float vigour = Math.min(health, maxHealth) / maxHealth;
    return Math.min(1.0F, resistPercent / 100.0F * strength * vigour);
  }
}
