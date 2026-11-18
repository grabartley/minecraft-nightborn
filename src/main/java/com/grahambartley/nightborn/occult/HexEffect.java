package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class HexEffect extends StatusEffect {

  static final int HEXED_COLOUR = 0x3B0A2A;

  public static final RegistryEntry<StatusEffect> HEXED =
      Registry.registerReference(
          Registries.STATUS_EFFECT, Identifier.of(Nightborn.MOD_ID, "hexed"), new HexEffect());

  private HexEffect() {
    super(StatusEffectCategory.HARMFUL, HEXED_COLOUR);
  }

  public static void register() {}
}
