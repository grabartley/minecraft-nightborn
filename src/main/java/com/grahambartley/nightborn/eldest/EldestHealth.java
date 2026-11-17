package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;

public final class EldestHealth {

  static final Identifier CROWD = Identifier.of(Nightborn.MOD_ID, "eldest/crowd");

  private EldestHealth() {}

  public static double bonus(final int players, final int percentPerPlayer) {
    return Math.max(0, players - 1) * Math.max(0, percentPerPlayer) / 100.0;
  }

  public static void scale(
      final LivingEntity eldest, final int players, final int percentPerPlayer) {
    Modifiers.set(
        eldest,
        EntityAttributes.GENERIC_MAX_HEALTH,
        CROWD,
        bonus(players, percentPerPlayer),
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    eldest.setHealth(eldest.getMaxHealth());
  }
}
