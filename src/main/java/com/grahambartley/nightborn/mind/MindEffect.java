package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class MindEffect extends StatusEffect {

  static final int MESMERISED_COLOUR = 0x6B1F5C;
  static final int CHARMED_COLOUR = 0xB08D3C;

  public static final RegistryEntry<StatusEffect> MESMERISED =
      register("mesmerised", StatusEffectCategory.HARMFUL, MESMERISED_COLOUR);
  public static final RegistryEntry<StatusEffect> CHARMED =
      register("charmed", StatusEffectCategory.NEUTRAL, CHARMED_COLOUR);

  private MindEffect(final StatusEffectCategory category, final int colour) {
    super(category, colour);
  }

  public static void register() {}

  private static RegistryEntry<StatusEffect> register(
      final String path, final StatusEffectCategory category, final int colour) {
    return Registry.registerReference(
        Registries.STATUS_EFFECT,
        Identifier.of(Nightborn.MOD_ID, path),
        new MindEffect(category, colour));
  }
}
