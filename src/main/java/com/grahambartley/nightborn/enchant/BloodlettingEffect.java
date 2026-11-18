package com.grahambartley.nightborn.enchant;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record BloodlettingEffect(EnchantmentLevelBasedValue blows)
    implements EnchantmentEntityEffect {

  public static final MapCodec<BloodlettingEffect> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      EnchantmentLevelBasedValue.CODEC
                          .fieldOf("blows")
                          .forGetter(BloodlettingEffect::blows))
                  .apply(instance, BloodlettingEffect::new));
  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "bloodletting");
  public static final AttachmentType<Integer> BLOWS =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "bloodletting_blows"));

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
    final Integer soFar = user.getAttached(BLOWS);
    final BloodlettingCount count =
        BloodlettingCount.after(soFar == null ? 0 : soFar, Math.round(this.blows.getValue(level)));
    user.setAttached(BLOWS, count.blows());
    if (count.bleeds()) {
      BloodAttachment.set(user, BloodAttachment.get(user) + 1);
    }
  }

  @Override
  public MapCodec<BloodlettingEffect> getCodec() {
    return CODEC;
  }
}
