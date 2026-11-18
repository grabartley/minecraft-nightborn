package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.block.Block;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

public final class PartyData {

  public static final RegistryKey<Registry<CampLayout>> CAMP =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "party_camp"));
  public static final TagKey<Biome> BIOMES =
      TagKey.of(RegistryKeys.BIOME, Identifier.of(Nightborn.MOD_ID, "hunter_parties"));
  public static final TagKey<Block> GROUND =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "camp_ground"));
  public static final TagKey<Block> CLEARABLE =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "camp_clearable"));

  private PartyData() {}

  public static void register() {
    DynamicRegistries.register(CAMP, CampLayout.CODEC);
  }

  public static Optional<CampLayout> layout(final ServerWorld world, final Random random) {
    final List<CampLayout> layouts = world.getRegistryManager().get(CAMP).stream().toList();
    return layouts.isEmpty()
        ? Optional.empty()
        : Optional.of(layouts.get(random.nextInt(layouts.size())));
  }
}
