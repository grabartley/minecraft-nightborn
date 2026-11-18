package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.vampire.Modifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;

public final class BossHealth {

  private BossHealth() {}

  public static double bonus(final int players, final int percentPerPlayer) {
    return Math.max(0, players - 1) * Math.max(0, percentPerPlayer) / 100.0;
  }

  public static void scale(
      final LivingEntity boss,
      final Identifier crowd,
      final int players,
      final int percentPerPlayer) {
    Modifiers.set(
        boss,
        EntityAttributes.GENERIC_MAX_HEALTH,
        crowd,
        bonus(players, percentPerPlayer),
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    boss.setHealth(boss.getMaxHealth());
  }
}
