package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
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
    final Identifier item = Registries.ITEM.getId(stack.getItem());
    return registries
        .getOptionalWrapper(KEY)
        .flatMap(accessories -> accessories.getOptional(RegistryKey.of(KEY, item)))
        .map(entry -> entry.value());
  }
}
