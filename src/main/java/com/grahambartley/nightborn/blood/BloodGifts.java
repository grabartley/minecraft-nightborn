package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import java.util.Comparator;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public final class BloodGifts {

  public static final RegistryKey<Registry<BloodGift>> KEY =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "blood_gift"));

  private BloodGifts() {}

  public static void register() {
    DynamicRegistries.registerSynced(KEY, BloodGift.CODEC);
  }

  public static Optional<Identifier> forPrey(
      final DynamicRegistryManager registries, final EntityType<?> prey) {
    return registries
        .getOptional(KEY)
        .flatMap(
            gifts ->
                gifts
                    .streamEntries()
                    .filter(entry -> entry.value().feeds(prey))
                    .map(entry -> entry.registryKey().getValue())
                    .min(Comparator.comparing(Identifier::toString)));
  }

  public static Optional<BloodGift> byId(
      final DynamicRegistryManager registries, final Identifier id) {
    return registries.getOptional(KEY).flatMap(gifts -> gifts.getOrEmpty(id));
  }

  public static Optional<BloodGift> byId(
      final RegistryWrapper.WrapperLookup registries, final Identifier id) {
    return registries
        .getOptionalWrapper(KEY)
        .flatMap(gifts -> gifts.getOptional(RegistryKey.of(KEY, id)))
        .map(entry -> entry.value());
  }
}
