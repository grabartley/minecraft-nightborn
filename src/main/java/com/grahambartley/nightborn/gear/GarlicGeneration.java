package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class GarlicGeneration {

  public static final RegistryKey<PlacedFeature> PATCH_GARLIC =
      RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Nightborn.MOD_ID, "patch_garlic"));
  public static final TagKey<Biome> GROWS_WILD_GARLIC =
      TagKey.of(RegistryKeys.BIOME, Identifier.of(Nightborn.MOD_ID, "grows_wild_garlic"));

  private GarlicGeneration() {}

  public static void register() {
    BiomeModifications.addFeature(
        BiomeSelectors.tag(GROWS_WILD_GARLIC),
        GenerationStep.Feature.VEGETAL_DECORATION,
        PATCH_GARLIC);
  }
}
