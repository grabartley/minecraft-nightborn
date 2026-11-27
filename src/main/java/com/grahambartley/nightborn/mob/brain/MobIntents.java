package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class MobIntents {

  public static final RegistryKey<Registry<IntentBudget>> BUDGET =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "mob_intent"));

  private MobIntents() {}

  public static void register() {
    DynamicRegistries.register(BUDGET, IntentBudget.CODEC);
  }

  public static Identifier id(final Intent intent) {
    return Identifier.of(Nightborn.MOD_ID, intent.key());
  }

  public static IntentBudget budget(final DynamicRegistryManager registries, final Intent intent) {
    return registries
        .getOptional(BUDGET)
        .map(registry -> registry.get(id(intent)))
        .orElse(IntentBudget.FREE);
  }
}
