package com.grahambartley.nightborn.feeding;

import java.util.Optional;

public enum LancetRules {
  BLEEDS(null),
  PASS(null),
  NO_BOTTLE("nightborn.lancet.no_bottle"),
  TOO_WEAK("nightborn.lancet.too_weak"),
  A_PLAYER("nightborn.lancet.player"),
  GUARDED("nightborn.lancet.guarded"),
  NO_WARM_BLOOD("nightborn.lancet.no_blood");

  private final String refusal;

  LancetRules(final String refusal) {
    this.refusal = refusal;
  }

  public record Creature(
      boolean warmBlood,
      boolean vampire,
      boolean player,
      boolean alive,
      float health,
      boolean guarded) {}

  public static float draw(final int lancetDamage, final int bottleSize) {
    return Math.max(lancetDamage, bottleSize);
  }

  public static LancetRules onCreature(
      final Creature creature, final boolean emptyBottle, final float draw) {
    if (creature.player()) {
      return A_PLAYER;
    }
    if (!creature.alive() || !BiteRules.drinkable(creature.warmBlood(), creature.vampire())) {
      return NO_WARM_BLOOD;
    }
    if (creature.guarded()) {
      return GUARDED;
    }
    return bleeds(creature.health(), emptyBottle, draw);
  }

  public static LancetRules onSelf(
      final boolean sneaking,
      final boolean vampire,
      final boolean emptyBottle,
      final float health,
      final float draw) {
    if (!sneaking || vampire) {
      return PASS;
    }
    return bleeds(health, emptyBottle, draw);
  }

  static LancetRules bleeds(final float health, final boolean emptyBottle, final float draw) {
    if (!emptyBottle) {
      return NO_BOTTLE;
    }
    return health - draw >= Drinking.LAST_HEALTH ? BLEEDS : TOO_WEAK;
  }

  public Optional<String> refusal() {
    return Optional.ofNullable(refusal);
  }
}
