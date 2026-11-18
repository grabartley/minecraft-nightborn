package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class WornPowers {

  public static final AttachmentType<PowerSources> TYPE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "worn_powers"));

  private WornPowers() {}

  public static void register() {}

  public static void wear(
      final LivingEntity wearer,
      final ClothingPower power,
      final String source,
      final double amount) {
    update(wearer, sources(wearer).with(power, source, amount));
  }

  public static void wear(final LivingEntity wearer, final String source, final ItemStack stack) {
    PowerSources next = sources(wearer).without(source);
    final ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
    for (final var entry : enchantments.getEnchantmentEntries()) {
      for (final EnchantmentEffectEntry<EnchantmentLocationBasedEffect> effect :
          entry.getKey().value().getEffect(EnchantmentEffectComponentTypes.LOCATION_CHANGED)) {
        if (effect.effect() instanceof ClothingPowerEffect power) {
          next =
              next.with(
                  power.power(),
                  source,
                  power.amount(entry.getIntValue(), ServerConfigService.current().clothing()));
        }
      }
    }
    update(wearer, next);
  }

  public static void takeOff(
      final LivingEntity wearer, final ClothingPower power, final String source) {
    update(wearer, sources(wearer).without(power, source));
  }

  public static void takeOff(final LivingEntity wearer, final String source) {
    update(wearer, sources(wearer).without(source));
  }

  public static Identifier modifierId(final ClothingPower power) {
    return Identifier.of(Nightborn.MOD_ID, "clothing/" + power.asString());
  }

  static Identifier legacyModifierId(final ClothingPower power, final EquipmentSlot slot) {
    return Identifier.of(Nightborn.MOD_ID, "clothing/" + power.asString() + "/" + slot.getName());
  }

  private static PowerSources sources(final LivingEntity wearer) {
    final PowerSources sources = wearer.getAttached(TYPE);
    return sources == null ? PowerSources.NONE : sources;
  }

  private static void update(final LivingEntity wearer, final PowerSources next) {
    wearer.setAttached(TYPE, next);
    for (final ClothingPower power : ClothingPower.values()) {
      final EntityAttributeInstance instance = wearer.getAttributeInstance(power.attribute());
      if (instance == null) {
        continue;
      }
      for (final EquipmentSlot slot : EquipmentSlot.values()) {
        instance.removeModifier(legacyModifierId(power, slot));
      }
      final double amount = next.strongest(power);
      final EntityAttributeModifier current = instance.getModifier(modifierId(power));
      if (current != null && current.value() == amount) {
        continue;
      }
      instance.removeModifier(modifierId(power));
      if (amount != 0.0) {
        instance.addTemporaryModifier(
            new EntityAttributeModifier(
                modifierId(power), amount, EntityAttributeModifier.Operation.ADD_VALUE));
      }
    }
  }
}
