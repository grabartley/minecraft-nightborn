package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.Nightborn;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class BodyRuleData {

  public static final RegistryKey<Registry<BodyRule>> RULES =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "body_rule"));

  private BodyRuleData() {}

  static void register() {
    DynamicRegistries.register(RULES, BodyRule.CODEC);
    ServerLifecycleEvents.SERVER_STARTED.register(
        server ->
            server
                .getRegistryManager()
                .getOptional(RULES)
                .ifPresent(
                    registry ->
                        unknown(registry.getIds(), Registries.ENTITY_TYPE::containsId)
                            .forEach(
                                id ->
                                    Nightborn.LOGGER.error(
                                        "Body rule {} names no entity type and is ignored", id))));
  }

  public static BodyRule rule(final DynamicRegistryManager registries, final EntityType<?> type) {
    final Identifier id = EntityType.getId(type);
    return registries
        .getOptional(RULES)
        .filter(registry -> registry.containsId(id))
        .map(registry -> registry.get(id))
        .orElse(BodyRule.DEFAULT);
  }

  static List<Identifier> unknown(
      final Collection<Identifier> ids, final Predicate<Identifier> entityType) {
    return ids.stream().filter(entityType.negate()).sorted().toList();
  }
}
