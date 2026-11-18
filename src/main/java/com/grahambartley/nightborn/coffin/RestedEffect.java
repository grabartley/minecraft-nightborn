package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class RestedEffect extends StatusEffect {

  static final int COLOUR = 0x8A6A2E;

  public static final RegistryEntry<StatusEffect> ENTRY =
      Registry.registerReference(
          Registries.STATUS_EFFECT, Identifier.of(Nightborn.MOD_ID, "rested"), new RestedEffect());

  private RestedEffect() {
    super(StatusEffectCategory.BENEFICIAL, COLOUR);
  }

  public static void register() {}
}
