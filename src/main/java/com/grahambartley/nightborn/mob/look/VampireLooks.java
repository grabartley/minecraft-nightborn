package com.grahambartley.nightborn.mob.look;

import com.grahambartley.nightborn.Nightborn;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class VampireLooks {

  public static final RegistryKey<Registry<VampireLook>> LOOK =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "vampire_look"));
  public static final RegistryKey<Registry<VampireOutfit>> OUTFIT =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "vampire_outfit"));
  public static final RegistryKey<Registry<VampireBuild>> BUILD =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "vampire_build"));

  public static final Identifier NONE = Identifier.of(Nightborn.MOD_ID, "none");

  private VampireLooks() {}

  public static void register() {
    DynamicRegistries.registerSynced(LOOK, VampireLook.CODEC);
    DynamicRegistries.registerSynced(OUTFIT, VampireOutfit.CODEC);
    DynamicRegistries.registerSynced(BUILD, VampireBuild.CODEC);
    ServerLifecycleEvents.SERVER_STARTED.register(
        server -> {
          final DynamicRegistryManager registries = server.getRegistryManager();
          LookDataCheck.problems(
                  looks(registries), outfits(registries), builds(registries), VampireLooks::slotOf)
              .forEach(problem -> Nightborn.LOGGER.error("Bad vampire look data: {}", problem));
        });
  }

  public static Map<Identifier, VampireLook> looks(final DynamicRegistryManager registries) {
    return copy(registries, LOOK);
  }

  public static Map<Identifier, VampireOutfit> outfits(final DynamicRegistryManager registries) {
    return copy(registries, OUTFIT);
  }

  public static Map<Identifier, VampireBuild> builds(final DynamicRegistryManager registries) {
    return copy(registries, BUILD);
  }

  public static Optional<VampireLook> look(
      final DynamicRegistryManager registries, final Identifier id) {
    return registries.getOptional(LOOK).map(registry -> registry.get(id));
  }

  public static Optional<Item> item(final Identifier id) {
    return Registries.ITEM.getOrEmpty(id);
  }

  static Optional<EquipmentSlot> slotOf(final Identifier id) {
    return item(id).flatMap(OutfitDressing::slot);
  }

  private static <T> Map<Identifier, T> copy(
      final DynamicRegistryManager registries, final RegistryKey<Registry<T>> key) {
    final Map<Identifier, T> entries = new HashMap<>();
    registries
        .getOptional(key)
        .ifPresent(registry -> registry.getIds().forEach(id -> entries.put(id, registry.get(id))));
    return entries;
  }
}
