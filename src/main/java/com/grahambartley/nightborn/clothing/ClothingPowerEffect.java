package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Modifiers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record ClothingPowerEffect(ClothingPower power) implements EnchantmentLocationBasedEffect {

  public static final MapCodec<ClothingPowerEffect> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(ClothingPower.CODEC.fieldOf("power").forGetter(ClothingPowerEffect::power))
                  .apply(instance, ClothingPowerEffect::new));
  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "clothing_power");

  public static void register() {
    Registry.register(Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, ID, CODEC);
  }

  public Identifier modifierId(final EquipmentSlot slot) {
    return Identifier.of(
        Nightborn.MOD_ID, "clothing/" + this.power.asString() + "/" + slot.getName());
  }

  @Override
  public void apply(
      final ServerWorld world,
      final int level,
      final EnchantmentEffectContext context,
      final Entity user,
      final Vec3d pos,
      final boolean newlyApplied) {
    if (user instanceof LivingEntity wearer) {
      Modifiers.set(
          wearer,
          this.power.attribute(),
          modifierId(context.slot()),
          this.power.amount(level, ServerConfigService.current().clothing()),
          EntityAttributeModifier.Operation.ADD_VALUE);
    }
  }

  @Override
  public void remove(
      final EnchantmentEffectContext context, final Entity user, final Vec3d pos, final int level) {
    if (user instanceof LivingEntity wearer) {
      Modifiers.remove(wearer, this.power.attribute(), modifierId(context.slot()));
    }
  }

  @Override
  public MapCodec<ClothingPowerEffect> getCodec() {
    return CODEC;
  }
}
