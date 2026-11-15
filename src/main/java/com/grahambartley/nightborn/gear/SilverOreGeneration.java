package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class SilverOreGeneration {

  public static final RegistryKey<PlacedFeature> ORE_SILVER =
      RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Nightborn.MOD_ID, "ore_silver"));

  private SilverOreGeneration() {}

  public static void register() {
    BiomeModifications.addFeature(
        BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, ORE_SILVER);
  }
}
