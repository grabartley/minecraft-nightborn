package com.grahambartley.nightborn.sickness;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class BiteSicknessEffect extends StatusEffect {

  static final int COLOUR = 0x5A0A14;

  public static final RegistryEntry<StatusEffect> ENTRY =
      Registry.registerReference(
          Registries.STATUS_EFFECT,
          Identifier.of(Nightborn.MOD_ID, "bite_sickness"),
          new BiteSicknessEffect());

  private BiteSicknessEffect() {
    super(StatusEffectCategory.HARMFUL, COLOUR);
  }

  public static void register() {}
}
