package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class BloodGiftEffect extends StatusEffect {

  static final int COLOUR = 0x7A1020;

  public static final RegistryEntry<StatusEffect> ENTRY =
      Registry.registerReference(
          Registries.STATUS_EFFECT,
          Identifier.of(Nightborn.MOD_ID, "blood_gift"),
          new BloodGiftEffect());

  private BloodGiftEffect() {
    super(StatusEffectCategory.BENEFICIAL, COLOUR);
  }

  public static void register() {}
}
