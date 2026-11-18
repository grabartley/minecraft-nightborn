package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class OccultAttributes {

  static final String FAMILIAR_LIMIT_PATH = "familiar_limit";
  static final double MAX_FAMILIARS = 8.0;
  static final String FAMILIAR_HEALTH_PATH = "familiar_health";
  static final double MOST_FAMILIAR_HEALTH = 2.0;

  public static final RegistryEntry<EntityAttribute> FAMILIAR_LIMIT =
      Registry.registerReference(
          Registries.ATTRIBUTE,
          Identifier.of(Nightborn.MOD_ID, FAMILIAR_LIMIT_PATH),
          new ClampedEntityAttribute(translationKey(FAMILIAR_LIMIT_PATH), 0.0, 0.0, MAX_FAMILIARS));

  public static final RegistryEntry<EntityAttribute> FAMILIAR_HEALTH =
      Registry.registerReference(
          Registries.ATTRIBUTE,
          Identifier.of(Nightborn.MOD_ID, FAMILIAR_HEALTH_PATH),
          new ClampedEntityAttribute(
              translationKey(FAMILIAR_HEALTH_PATH), 0.0, 0.0, MOST_FAMILIAR_HEALTH));

  private OccultAttributes() {}

  public static void register() {}

  public static DefaultAttributeContainer.Builder addTo(
      final DefaultAttributeContainer.Builder builder) {
    return builder.add(FAMILIAR_LIMIT).add(FAMILIAR_HEALTH);
  }

  public static int familiarLimit(final LivingEntity entity) {
    return entity.getAttributes().hasAttribute(FAMILIAR_LIMIT)
        ? (int) Math.floor(entity.getAttributeValue(FAMILIAR_LIMIT))
        : 0;
  }

  public static double familiarHealth(final LivingEntity entity) {
    return 1.0
        + (entity.getAttributes().hasAttribute(FAMILIAR_HEALTH)
            ? entity.getAttributeValue(FAMILIAR_HEALTH)
            : 0.0);
  }

  static String translationKey(final String path) {
    return "attribute.name." + Nightborn.MOD_ID + "." + path;
  }
}
