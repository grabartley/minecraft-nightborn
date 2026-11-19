package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.core.DataEntries;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class RankData {

  public static final RegistryKey<Registry<GuildRank>> RANK =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "guild_rank"));

  private RankData() {}

  static void register() {
    DynamicRegistries.registerSynced(RANK, GuildRank.CODEC);
  }

  public static Map<Identifier, GuildRank> ranks(final DynamicRegistryManager registries) {
    return DataEntries.copy(registries, RANK);
  }

  public static Optional<GuildRank> rank(
      final DynamicRegistryManager registries, final Identifier id) {
    return registries.getOptional(RANK).flatMap(registry -> registry.getOrEmpty(id));
  }
}
