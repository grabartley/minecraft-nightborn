package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class SanctuaryEffect extends StatusEffect {

  static final int COLOUR = 0xF2E3A6;

  public static final RegistryEntry<StatusEffect> ENTRY =
      Registry.registerReference(
          Registries.STATUS_EFFECT,
          Identifier.of(Nightborn.MOD_ID, "sanctuary"),
          new SanctuaryEffect());

  private SanctuaryEffect() {
    super(StatusEffectCategory.BENEFICIAL, COLOUR);
  }

  public static void register() {}
}
