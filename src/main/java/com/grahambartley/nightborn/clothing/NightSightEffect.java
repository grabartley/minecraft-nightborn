package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.core.NightSight;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class NightSightEffect implements EnchantmentEntityEffect {

  public static final NightSightEffect INSTANCE = new NightSightEffect();
  public static final MapCodec<NightSightEffect> CODEC = MapCodec.unit(INSTANCE);
  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "night_sight");

  private NightSightEffect() {}

  public static void register() {
    Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, ID, CODEC);
  }

  @Override
  public void apply(
      final ServerWorld world,
      final int level,
      final EnchantmentEffectContext context,
      final Entity user,
      final Vec3d pos) {
    if (user instanceof LivingEntity seer
        && ServerConfigService.current().clothing().nocturne()
        && NightSight.isNight(world)) {
      NightSight.keep(seer);
    }
  }

  @Override
  public MapCodec<NightSightEffect> getCodec() {
    return CODEC;
  }
}
