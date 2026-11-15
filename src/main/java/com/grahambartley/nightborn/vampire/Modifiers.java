package com.grahambartley.nightborn.vampire;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class Modifiers {

  private Modifiers() {}

  public static void set(
      final LivingEntity entity,
      final RegistryEntry<EntityAttribute> attribute,
      final Identifier id,
      final double value,
      final EntityAttributeModifier.Operation operation) {
    final EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
    if (instance == null) {
      return;
    }
    if (value == 0.0) {
      instance.removeModifier(id);
      return;
    }
    final EntityAttributeModifier current = instance.getModifier(id);
    if (current != null && current.value() == value && current.operation() == operation) {
      return;
    }
    instance.overwritePersistentModifier(new EntityAttributeModifier(id, value, operation));
  }

  public static void remove(
      final LivingEntity entity,
      final RegistryEntry<EntityAttribute> attribute,
      final Identifier id) {
    final EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
    if (instance != null) {
      instance.removeModifier(id);
    }
  }
}
