package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public final class GuildData {

  public static final RegistryKey<Registry<GuildOffer>> OFFER =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "guild_offer"));
  public static final RegistryKey<Registry<HunterContract>> CONTRACT =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "hunter_contract"));

  private GuildData() {}

  static void register() {
    DynamicRegistries.register(OFFER, GuildOffer.CODEC);
    DynamicRegistries.registerSynced(CONTRACT, HunterContract.CODEC);
  }

  public static List<GuildOffer> offers(final DynamicRegistryManager registries) {
    final Map<Identifier, GuildOffer> offers = new HashMap<>();
    registries
        .getOptional(OFFER)
        .ifPresent(registry -> registry.getIds().forEach(id -> offers.put(id, registry.get(id))));
    return GuildOffer.inOrder(offers);
  }

  public static Optional<HunterContract> contract(
      final RegistryWrapper.WrapperLookup registries, final Identifier id) {
    return registries
        .getOptionalWrapper(CONTRACT)
        .flatMap(wrapper -> wrapper.getOptional(RegistryKey.of(CONTRACT, id)))
        .map(entry -> entry.value());
  }
}
