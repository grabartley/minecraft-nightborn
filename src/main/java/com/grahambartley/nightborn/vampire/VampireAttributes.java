package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class VampireAttributes {

  public static final double MOST_REDUCED = 0.2;
  public static final double MOST_EXTRA_BLOOD = 40.0;
  public static final double MOST_PREY_SENSE = 64.0;
  public static final double MOST_HEALING_SPEED = 4.0;

  public static final RegistryEntry<EntityAttribute> DARK_SIGHT =
      register("dark_sight", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> GLOOM_RESISTANCE =
      register("gloom_resistance", 0.0, 0.0, 1.0 - MOST_REDUCED);
  public static final RegistryEntry<EntityAttribute> FOG_SIGHT =
      register("fog_sight", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> VICTIM_STRUGGLE =
      register("victim_struggle", 1.0, MOST_REDUCED, 1.0);
  public static final RegistryEntry<EntityAttribute> FEEDING_SPEED =
      register("feeding_speed", 1.0, 1.0, 4.0);
  public static final RegistryEntry<EntityAttribute> RESTRAINT_BREAK_SPEED =
      register("restraint_break_speed", 1.0, 1.0, 4.0);
  public static final RegistryEntry<EntityAttribute> EXTRA_BLOOD =
      register("extra_blood", 0.0, 0.0, MOST_EXTRA_BLOOD);
  public static final RegistryEntry<EntityAttribute> DOWNED_TIME =
      register("downed_time", 1.0, MOST_REDUCED, 1.0);
  public static final RegistryEntry<EntityAttribute> RISE_HEALTH =
      register("rise_health", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> POWER_COOLDOWN =
      register("power_cooldown", 1.0, MOST_REDUCED, 1.0);
  public static final RegistryEntry<EntityAttribute> PREY_SENSE =
      register("prey_sense", 0.0, 0.0, MOST_PREY_SENSE);
  public static final RegistryEntry<EntityAttribute> HEALING_SPEED =
      register("healing_speed", 0.0, 0.0, MOST_HEALING_SPEED);
  public static final RegistryEntry<EntityAttribute> POTION_RESISTANCE =
      register("potion_resistance", 0.0, 0.0, 1.0);

  public static final List<RegistryEntry<EntityAttribute>> ALL =
      List.of(
          DARK_SIGHT,
          GLOOM_RESISTANCE,
          FOG_SIGHT,
          VICTIM_STRUGGLE,
          FEEDING_SPEED,
          RESTRAINT_BREAK_SPEED,
          EXTRA_BLOOD,
          DOWNED_TIME,
          RISE_HEALTH,
          POWER_COOLDOWN,
          PREY_SENSE,
          HEALING_SPEED,
          POTION_RESISTANCE);

  private VampireAttributes() {}

  public static void register() {}

  public static DefaultAttributeContainer.Builder addTo(
      final DefaultAttributeContainer.Builder builder) {
    ALL.forEach(builder::add);
    return builder;
  }

  public static double value(
      final LivingEntity entity, final RegistryEntry<EntityAttribute> attribute) {
    return entity.getAttributes().hasAttribute(attribute)
        ? entity.getAttributeValue(attribute)
        : attribute.value().getDefaultValue();
  }

  public static boolean has(
      final LivingEntity entity, final RegistryEntry<EntityAttribute> attribute) {
    return value(entity, attribute) > 0.0;
  }

  public static String translationKey(final String path) {
    return "attribute.name." + Nightborn.MOD_ID + "." + path;
  }

  private static RegistryEntry<EntityAttribute> register(
      final String path, final double base, final double min, final double max) {
    return Registry.registerReference(
        Registries.ATTRIBUTE,
        Identifier.of(Nightborn.MOD_ID, path),
        new ClampedEntityAttribute(translationKey(path), base, min, max).setTracked(true));
  }
}
