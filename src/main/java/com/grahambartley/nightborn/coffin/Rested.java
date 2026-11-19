package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.CoffinSettings;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

public final class Rested {

  public static final Identifier SPEED = Identifier.of(Nightborn.MOD_ID, "rested_speed");
  static final Identifier DAMAGE = Identifier.of(Nightborn.MOD_ID, "rested_damage");
  static final int NOT_RESTED = -1;

  private Rested() {}

  public static void tick(final LivingEntity vampire, final CoffinSettings settings) {
    final StatusEffectInstance rested = vampire.getStatusEffect(RestedEffect.ENTRY);
    final RestedBoost boost =
        RestedBoost.of(settings, rested == null ? NOT_RESTED : rested.getAmplifier());
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_MOVEMENT_SPEED,
        SPEED,
        boost.speedMultiplier(),
        Operation.ADD_MULTIPLIED_TOTAL);
    Modifiers.set(
        vampire,
        EntityAttributes.GENERIC_ATTACK_DAMAGE,
        DAMAGE,
        boost.attackDamage(),
        Operation.ADD_VALUE);
  }

  public static void remove(final LivingEntity entity) {
    entity.removeStatusEffect(RestedEffect.ENTRY);
    Modifiers.remove(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED);
    Modifiers.remove(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE);
  }
}
