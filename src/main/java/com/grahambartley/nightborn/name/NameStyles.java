package com.grahambartley.nightborn.name;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class NameStyles {

  public static final RegistryKey<Registry<NameStyle>> STYLE =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "name_style"));

  private NameStyles() {}

  public static void register() {
    DynamicRegistries.registerSynced(STYLE, NameStyle.CODEC);
  }

  public static Optional<NameStyle> of(final Entity entity) {
    return entity
        .getRegistryManager()
        .getOptional(STYLE)
        .flatMap(registry -> registry.getOrEmpty(Registries.ENTITY_TYPE.getId(entity.getType())));
  }
}
