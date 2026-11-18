package com.grahambartley.nightborn.clothing;

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

public final class WornAttributes {

  public static final double MOST_RESISTANCE = 0.8;
  public static final double LEAST_SHARE = 0.2;
  public static final double MOST_SIGHT = 2.0;
  public static final double MOST_REACH = 8.0;
  public static final double MOST_MULTIPLIER = 3.0;

  public static final RegistryEntry<EntityAttribute> NOTICE_DISTANCE =
      register("notice_distance", 1.0, LEAST_SHARE, MOST_SIGHT);
  public static final RegistryEntry<EntityAttribute> HUNTER_SIGHT =
      register("hunter_sight", 1.0, 1.0, MOST_SIGHT);
  public static final RegistryEntry<EntityAttribute> VANISH_REACH =
      register("vanish_reach", 0.0, 0.0, MOST_REACH);
  public static final RegistryEntry<EntityAttribute> WARD_RESISTANCE =
      register("ward_resistance", 0.0, 0.0, MOST_RESISTANCE);
  public static final RegistryEntry<EntityAttribute> BLOOD_DRAIN =
      register("blood_drain", 1.0, LEAST_SHARE, MOST_MULTIPLIER);
  public static final RegistryEntry<EntityAttribute> SUN_DAMAGE =
      register("sun_damage", 1.0, 1.0, MOST_MULTIPLIER);

  public static final List<RegistryEntry<EntityAttribute>> ALL =
      List.of(
          NOTICE_DISTANCE, HUNTER_SIGHT, VANISH_REACH, WARD_RESISTANCE, BLOOD_DRAIN, SUN_DAMAGE);

  private WornAttributes() {}

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
