package com.grahambartley.nightborn.balance;

import java.util.EnumMap;
import java.util.Map;

public final class Tally {

  public record Blows(int meleeHits, float meleeDamage, int otherHits, float otherDamage) {

    static final Blows NONE = new Blows(0, 0.0F, 0, 0.0F);

    Blows add(final boolean melee, final float damage) {
      return melee
          ? new Blows(meleeHits + 1, meleeDamage + damage, otherHits, otherDamage)
          : new Blows(meleeHits, meleeDamage, otherHits + 1, otherDamage + damage);
    }

    Blows plus(final Blows more) {
      return new Blows(
          meleeHits + more.meleeHits,
          meleeDamage + more.meleeDamage,
          otherHits + more.otherHits,
          otherDamage + more.otherDamage);
    }

    public float damage() {
      return meleeDamage + otherDamage;
    }

    public double meleeShare() {
      return damage() <= 0.0F ? 0.0 : meleeDamage / damage();
    }
  }

  private final Map<Side, Blows> blows = new EnumMap<>(Side.class);

  public void hit(final Side attacker, final boolean melee, final float damage) {
    if (damage > 0.0F) {
      blows.put(attacker, of(attacker).add(melee, damage));
    }
  }

  public Blows of(final Side side) {
    return blows.getOrDefault(side, Blows.NONE);
  }
}
