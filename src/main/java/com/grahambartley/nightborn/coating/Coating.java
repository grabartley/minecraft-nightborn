package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.config.CoatingSettings;
import com.grahambartley.nightborn.config.IntSetting;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.ward.Wards;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;

public enum Coating implements StringIdentifiable {
  HOLY_WATER(
      "holy_water",
      HolyWater.WEAKENED_SOURCE,
      CoatingSettings.HOLY_WATER_DAMAGE,
      CoatingSettings.HOLY_WATER_HITS,
      CoatingSettings.HOLY_WATER_SECONDS,
      CoatingSettings.HOLY_WATER_STRENGTH,
      CoatingSettings.HOLY_WATER_WEAKEN_SECONDS),
  GARLIC(
      "garlic",
      Wards.TOUCH,
      CoatingSettings.GARLIC_DAMAGE,
      CoatingSettings.GARLIC_HITS,
      CoatingSettings.GARLIC_SECONDS,
      CoatingSettings.GARLIC_STRENGTH,
      CoatingSettings.GARLIC_WEAKEN_SECONDS);

  public static final Codec<Coating> CODEC = StringIdentifiable.createCodec(Coating::values);
  public static final PacketCodec<ByteBuf, Coating> PACKET_CODEC =
      PacketCodecs.indexed(index -> values()[index], Coating::ordinal);

  private final String id;
  private final String weakenedSource;
  final IntSetting damage;
  final IntSetting hits;
  final IntSetting seconds;
  final IntSetting strength;
  final IntSetting weakenSeconds;

  Coating(
      final String id,
      final String weakenedSource,
      final IntSetting damage,
      final IntSetting hits,
      final IntSetting seconds,
      final IntSetting strength,
      final IntSetting weakenSeconds) {
    this.id = id;
    this.weakenedSource = weakenedSource;
    this.damage = damage;
    this.hits = hits;
    this.seconds = seconds;
    this.strength = strength;
    this.weakenSeconds = weakenSeconds;
  }

  @Override
  public String asString() {
    return id;
  }

  public String weakenedSource() {
    return weakenedSource;
  }

  public SoundEvent wearOffSound() {
    return this == GARLIC
        ? SoundEvents.ENTITY_SPLASH_POTION_BREAK
        : SoundEvents.BLOCK_FIRE_EXTINGUISH;
  }

  public ParticleEffect wearOffParticle() {
    return this == GARLIC
        ? new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(GearItems.GARLIC))
        : ParticleTypes.WHITE_SMOKE;
  }

  public String translationKey() {
    return "nightborn.coating." + id;
  }
}
