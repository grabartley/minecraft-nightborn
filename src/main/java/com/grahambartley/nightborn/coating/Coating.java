package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.ammo.GarlicBombEntity;
import com.grahambartley.nightborn.config.CoatingSettings;
import com.grahambartley.nightborn.config.IntSetting;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.ward.Wards;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;

public enum Coating implements StringIdentifiable {
  HOLY_WATER(
      "holy_water",
      HolyWater.WEAKENED_SOURCE,
      HolyWater::sizzle,
      CoatingSettings.HOLY_WATER_DAMAGE,
      CoatingSettings.HOLY_WATER_HITS,
      CoatingSettings.HOLY_WATER_SECONDS,
      CoatingSettings.HOLY_WATER_STRENGTH,
      CoatingSettings.HOLY_WATER_WEAKEN_SECONDS),
  GARLIC(
      "garlic",
      Wards.TOUCH,
      (world, at) -> GarlicBombEntity.splatter(world, at.getX(), at.getBodyY(0.5), at.getZ()),
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
  private final BiConsumer<ServerWorld, Entity> wearOff;
  final IntSetting damage;
  final IntSetting hits;
  final IntSetting seconds;
  final IntSetting strength;
  final IntSetting weakenSeconds;

  Coating(
      final String id,
      final String weakenedSource,
      final BiConsumer<ServerWorld, Entity> wearOff,
      final IntSetting damage,
      final IntSetting hits,
      final IntSetting seconds,
      final IntSetting strength,
      final IntSetting weakenSeconds) {
    this.id = id;
    this.weakenedSource = weakenedSource;
    this.wearOff = wearOff;
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

  public void wearOff(final ServerWorld world, final Entity at) {
    wearOff.accept(world, at);
  }

  public String translationKey() {
    return "nightborn.coating." + id;
  }
}
