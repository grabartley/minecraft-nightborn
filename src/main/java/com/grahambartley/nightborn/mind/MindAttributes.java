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
  public static final double MOST_TITHE_SHARE = 0.8;
  static final double MOST_TITHE_RATE = 60.0;
  static final double MOST_VIGOUR = 1.0;
  static final double MOST_TITHE_RANGE = 32.0;

  public static final RegistryEntry<EntityAttribute> CHARM = register("charm", MAX_CHARM);
  public static final RegistryEntry<EntityAttribute> THRALL_LIMIT =
      register("thrall_limit", MAX_THRALLS);

  public static final RegistryEntry<EntityAttribute> TITHE_SHARE =
      register("tithe_share", MOST_TITHE_SHARE);
  public static final RegistryEntry<EntityAttribute> TITHE_RATE =
      register("tithe_rate", MOST_TITHE_RATE);
  public static final RegistryEntry<EntityAttribute> TITHE_RANGE =
      register("tithe_range", MOST_TITHE_RANGE);
  public static final RegistryEntry<EntityAttribute> SERVANT_VIGOUR =
      register("servant_vigour", MOST_VIGOUR);

  public static final List<RegistryEntry<EntityAttribute>> ALL =
      List.of(CHARM, THRALL_LIMIT, TITHE_SHARE, TITHE_RATE, TITHE_RANGE, SERVANT_VIGOUR);

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

  public static double value(
      final LivingEntity entity, final RegistryEntry<EntityAttribute> attribute) {
    return entity.getAttributes().hasAttribute(attribute)
        ? entity.getAttributeValue(attribute)
        : 0.0;
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
