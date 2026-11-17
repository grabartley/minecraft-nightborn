package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class BossFights {

  public static final RegistryKey<Registry<BossFightPlan>> FIGHT =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "boss_fight"));
  public static final RegistryKey<Registry<BossAttack>> ATTACK =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "boss_attack"));

  private BossFights() {}

  public static void register() {
    DynamicRegistries.register(FIGHT, BossFightPlan.CODEC);
    DynamicRegistries.register(ATTACK, BossAttack.CODEC);
    BossWard.register();
    ServerLifecycleEvents.SERVER_STARTED.register(server -> check(server.getRegistryManager()));
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
        (server, resources, success) -> check(server.getRegistryManager()));
  }

  private static void check(final DynamicRegistryManager registries) {
    registries
        .getOptional(FIGHT)
        .ifPresent(
            fights ->
                fights
                    .getIds()
                    .forEach(
                        id ->
                            missing(fights.get(id), attackId -> attack(registries, attackId))
                                .forEach(
                                    lost ->
                                        Nightborn.LOGGER.error(
                                            "Boss fight {} names a missing attack {}", id, lost))));
  }

  static List<Identifier> missing(
      final BossFightPlan plan, final Function<Identifier, Optional<BossAttack>> lookup) {
    return plan.phases().stream()
        .flatMap(phase -> phase.attacks().stream())
        .distinct()
        .filter(id -> lookup.apply(id).isEmpty())
        .toList();
  }

  public static Optional<BossFightPlan> fight(
      final DynamicRegistryManager registries, final Identifier id) {
    return registries.getOptional(FIGHT).flatMap(registry -> registry.getOrEmpty(id));
  }

  public static Optional<BossAttack> attack(
      final DynamicRegistryManager registries, final Identifier id) {
    return registries.getOptional(ATTACK).flatMap(registry -> registry.getOrEmpty(id));
  }
}
