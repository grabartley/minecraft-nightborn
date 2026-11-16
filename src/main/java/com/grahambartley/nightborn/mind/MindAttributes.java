package com.grahambartley.nightborn.mind;

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

public final class MindAttributes {

  static final double MAX_CHARM = 5.0;
  static final double MAX_THRALLS = 16.0;

  public static final RegistryEntry<EntityAttribute> CHARM = register("charm", MAX_CHARM);
  public static final RegistryEntry<EntityAttribute> THRALL_LIMIT =
      register("thrall_limit", MAX_THRALLS);

  public static final List<RegistryEntry<EntityAttribute>> ALL = List.of(CHARM, THRALL_LIMIT);

  private MindAttributes() {}

  public static void register() {}

  public static DefaultAttributeContainer.Builder addTo(
      final DefaultAttributeContainer.Builder builder) {
    ALL.forEach(builder::add);
    return builder;
  }

  public static int charm(final LivingEntity entity) {
    return level(entity, CHARM);
  }

  public static int thrallLimit(final LivingEntity entity) {
    return level(entity, THRALL_LIMIT);
  }

  static int level(final LivingEntity entity, final RegistryEntry<EntityAttribute> attribute) {
    return entity.getAttributes().hasAttribute(attribute)
        ? (int) Math.floor(entity.getAttributeValue(attribute))
        : 0;
  }

  public static String translationKey(final String path) {
    return "attribute.name." + Nightborn.MOD_ID + "." + path;
  }

  private static RegistryEntry<EntityAttribute> register(final String path, final double max) {
    return Registry.registerReference(
        Registries.ATTRIBUTE,
        Identifier.of(Nightborn.MOD_ID, path),
        new ClampedEntityAttribute(translationKey(path), 0.0, 0.0, max));
  }
}
