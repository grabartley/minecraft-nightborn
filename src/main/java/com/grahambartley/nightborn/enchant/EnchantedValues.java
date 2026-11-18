package com.grahambartley.nightborn.enchant;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class EnchantedValues {

  public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>>
      STAKE_DAMAGE = register("stake_damage");
  public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>>
      COATING_HITS = register("coating_hits");
  public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>>
      FINISHER_HEALTH = register("finisher_health");

  private static final Random RANDOM = Random.create();

  private EnchantedValues() {}

  public static void register() {}

  public static float apply(
      final ItemStack stack,
      final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> type,
      final float base) {
    final ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
    float value = base;
    for (final var entry : enchantments.getEnchantmentEntries()) {
      for (final EnchantmentEffectEntry<EnchantmentValueEffect> effect :
          entry.getKey().value().getEffect(type)) {
        value = effect.effect().apply(entry.getIntValue(), RANDOM, value);
      }
    }
    return value;
  }

  private static ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> register(
      final String path) {
    return Registry.register(
        Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
        Identifier.of(Nightborn.MOD_ID, path),
        ComponentType.<List<EnchantmentEffectEntry<EnchantmentValueEffect>>>builder()
            .codec(
                EnchantmentEffectEntry.createCodec(
                        EnchantmentValueEffect.CODEC, LootContextTypes.ENCHANTED_ITEM)
                    .listOf())
            .build());
  }
}
