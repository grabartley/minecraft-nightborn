package com.grahambartley.nightborn.hunter;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class HunterAttributes {

  static final double MAX_RESISTANCE = 1.0;
  static final double MAX_FAITH = 10.0;

  public static final RegistryEntry<EntityAttribute> BITE_RESISTANCE =
      register("bite_resistance", MAX_RESISTANCE);
  public static final RegistryEntry<EntityAttribute> MESMERISE_RESISTANCE =
      register("mesmerise_resistance", MAX_RESISTANCE);
  public static final RegistryEntry<EntityAttribute> FAITH = register("faith", MAX_FAITH);

  public static final List<RegistryEntry<EntityAttribute>> ALL =
      List.of(BITE_RESISTANCE, MESMERISE_RESISTANCE, FAITH);

  private HunterAttributes() {}

  public static void register() {}

  public static DefaultAttributeContainer.Builder addTo(
      final DefaultAttributeContainer.Builder builder) {
    ALL.forEach(builder::add);
    return builder;
  }

  public static double faith(final Entity entity) {
    return entity instanceof LivingEntity living && living.getAttributes().hasAttribute(FAITH)
        ? living.getAttributeValue(FAITH)
        : 0.0;
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
