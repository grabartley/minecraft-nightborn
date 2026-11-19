package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.core.DataEntries;
import java.util.Map;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class ChallengeData {

  public static final RegistryKey<Registry<Trial>> TRIAL =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "trial"));
  public static final RegistryKey<Registry<Feat>> FEAT =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "feat"));

  private ChallengeData() {}

  static void register() {
    DynamicRegistries.registerSynced(TRIAL, Trial.CODEC);
    DynamicRegistries.registerSynced(FEAT, Feat.CODEC);
  }

  public static Map<Identifier, Trial> trials(final DynamicRegistryManager registries) {
    return DataEntries.copy(registries.getOptional(TRIAL));
  }

  public static Map<Identifier, Feat> feats(final DynamicRegistryManager registries) {
    return DataEntries.copy(registries.getOptional(FEAT));
  }
}
