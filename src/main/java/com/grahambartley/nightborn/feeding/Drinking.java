package com.grahambartley.nightborn.feeding;

public final class Drinking {

  public static final float LAST_HEALTH = 1.0F;

  private Drinking() {}

  public record Sip(float take, boolean drains, boolean atLastHealth) {}

  public static Sip next(
      final float health, final float drinkPerSecond, final boolean wasAtLastHealth) {
    if (health <= 0.0F) {
      return new Sip(0.0F, false, false);
    }
    if (health <= LAST_HEALTH) {
      return wasAtLastHealth ? new Sip(health, true, false) : new Sip(0.0F, false, true);
    }
    final float take = Math.min(drinkPerSecond, health - LAST_HEALTH);
    return new Sip(take, false, health - take <= LAST_HEALTH);
  }
}
