package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.ClothingSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record ClothingPowerEffect(ClothingPower power, Optional<EnchantmentLevelBasedValue> value)
    implements EnchantmentLocationBasedEffect {

  public static final MapCodec<ClothingPowerEffect> CODEC =
      RecordCodecBuilder.<ClothingPowerEffect>mapCodec(
              instance ->
                  instance
                      .group(
                          ClothingPower.CODEC
                              .fieldOf("power")
                              .forGetter(ClothingPowerEffect::power),
                          EnchantmentLevelBasedValue.CODEC
                              .optionalFieldOf("amount")
                              .forGetter(ClothingPowerEffect::value))
                      .apply(instance, ClothingPowerEffect::new))
          .validate(ClothingPowerEffect::measured);
  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "clothing_power");
  static final String SLOT_SOURCE = "slot/";

  public static void register() {
    Registry.register(Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, ID, CODEC);
  }

  public static String source(final String slot) {
    return SLOT_SOURCE + slot;
  }

  public double amount(final int level, final ClothingSettings settings) {
    return this.value
        .map(value -> (double) value.getValue(level))
        .or(() -> this.power.amount(level, settings))
        .orElse(0.0);
  }

  @Override
  public void apply(
      final ServerWorld world,
      final int level,
      final EnchantmentEffectContext context,
      final Entity user,
      final Vec3d pos,
      final boolean newlyApplied) {
    if (user instanceof LivingEntity wearer && context.slot() != null) {
      WornPowers.wear(
          wearer,
          this.power,
          source(context.slot().getName()),
          amount(level, ServerConfigService.current().clothing()));
    }
  }

  @Override
  public void remove(
      final EnchantmentEffectContext context, final Entity user, final Vec3d pos, final int level) {
    if (user instanceof LivingEntity wearer && context.slot() != null) {
      WornPowers.takeOff(wearer, this.power, source(context.slot().getName()));
    }
  }

  @Override
  public MapCodec<ClothingPowerEffect> getCodec() {
    return CODEC;
  }

  private static DataResult<ClothingPowerEffect> measured(final ClothingPowerEffect effect) {
    return effect.value.isPresent()
            || effect.power.amount(1, NightbornConfig.DEFAULTS.clothing()).isPresent()
        ? DataResult.success(effect)
        : DataResult.error(() -> "Clothing power " + effect.power.asString() + " needs an amount");
  }
}
