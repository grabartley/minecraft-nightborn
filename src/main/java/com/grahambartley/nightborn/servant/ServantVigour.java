package com.grahambartley.nightborn.servant;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;

public final class ServantVigour {

  static final Identifier MODIFIER = Identifier.of(Nightborn.MOD_ID, "servant/vigour");

  private ServantVigour() {}

  public static void apply(final LivingEntity servant, final double vigour) {
    final double bonus = Math.max(0.0, vigour);
    Modifiers.set(
        servant,
        EntityAttributes.GENERIC_MAX_HEALTH,
        MODIFIER,
        bonus,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    Modifiers.set(
        servant,
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        MODIFIER,
        bonus,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    Modifiers.clampHealth(servant);
  }
}
