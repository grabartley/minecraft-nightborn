package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;

public final class StageStats {

  static final Identifier HEALTH = Identifier.of(Nightborn.MOD_ID, "stage/health");
  static final Identifier SPEED = Identifier.of(Nightborn.MOD_ID, "stage/speed");
  static final Identifier DAMAGE = Identifier.of(Nightborn.MOD_ID, "stage/damage");

  private StageStats() {}

  public static void apply(final LivingEntity vampire, final StageBody body) {
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_MAX_HEALTH,
        HEALTH,
        body.health(),
        EntityAttributeModifier.Operation.ADD_VALUE);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_MOVEMENT_SPEED,
        SPEED,
        body.speed(),
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        DAMAGE,
        body.damage(),
        EntityAttributeModifier.Operation.ADD_VALUE);
    Modifiers.clampHealth(vampire);
  }
}
