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

  public static final RegistryEntry<EntityAttribute> FAMILIAR_LIMIT =
      Registry.registerReference(
          Registries.ATTRIBUTE,
          Identifier.of(Nightborn.MOD_ID, FAMILIAR_LIMIT_PATH),
          new ClampedEntityAttribute(translationKey(), 0.0, 0.0, MAX_FAMILIARS));

  private OccultAttributes() {}

  public static void register() {}

  public static DefaultAttributeContainer.Builder addTo(
      final DefaultAttributeContainer.Builder builder) {
    return builder.add(FAMILIAR_LIMIT);
  }

  public static int familiarLimit(final LivingEntity entity) {
    return entity.getAttributes().hasAttribute(FAMILIAR_LIMIT)
        ? (int) Math.floor(entity.getAttributeValue(FAMILIAR_LIMIT))
        : 0;
  }

  static String translationKey() {
    return "attribute.name." + Nightborn.MOD_ID + "." + FAMILIAR_LIMIT_PATH;
  }
}
