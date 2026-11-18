package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public final class Accessories {

  public static final RegistryKey<Registry<Accessory>> KEY =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "accessory"));

  private Accessories() {}

  public static void register() {
    DynamicRegistries.registerSynced(KEY, Accessory.CODEC);
  }

  public static Optional<Accessory> of(
      final RegistryWrapper.WrapperLookup registries, final ItemStack stack) {
    return of(registries, stack.getItem());
  }

  public static Optional<Accessory> of(
      final RegistryWrapper.WrapperLookup registries, final Item item) {
    if (registries == null) {
      return Optional.empty();
    }
    final Identifier id = Registries.ITEM.getId(item);
    return registries
        .getOptionalWrapper(KEY)
        .flatMap(accessories -> accessories.getOptional(RegistryKey.of(KEY, id)))
        .map(entry -> entry.value());
  }
}
