package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.config.ClothingSettings;
import com.mojang.serialization.Codec;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.StringIdentifiable;

public enum ClothingPower implements StringIdentifiable {
  BATWING(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE),
  GLIDING_STEP(EntityAttributes.GENERIC_STEP_HEIGHT),
  HUSH(EntityAttributes.PLAYER_SNEAKING_SPEED),
  SHROUD(WornAttributes.NOTICE_DISTANCE),
  MISTCLOAK(WornAttributes.VANISH_REACH),
  WARDING(WornAttributes.WARD_RESISTANCE),
  THRIFT(WornAttributes.BLOOD_DRAIN),
  THIRST(WornAttributes.BLOOD_DRAIN),
  SUNMARKED(WornAttributes.SUN_DAMAGE),
  BEACON(WornAttributes.HUNTER_SIGHT);

  public static final Codec<ClothingPower> CODEC =
      StringIdentifiable.createCodec(ClothingPower::values);
  static final double TENTH = 0.1;
  static final double PERCENT = 0.01;

  private final RegistryEntry<EntityAttribute> attribute;

  ClothingPower(final RegistryEntry<EntityAttribute> attribute) {
    this.attribute = attribute;
  }

  public RegistryEntry<EntityAttribute> attribute() {
    return this.attribute;
  }

  public Optional<Double> amount(final int level, final ClothingSettings settings) {
    return switch (this) {
      case BATWING -> Optional.of((double) level * settings.batwingBlocks());
      case GLIDING_STEP -> Optional.of(settings.glidingStepTenths() * TENTH);
      case HUSH -> Optional.of(level * settings.hushSpeed() * PERCENT);
      case SHROUD -> Optional.of(-level * settings.shroudPercent() * PERCENT);
      case MISTCLOAK -> Optional.of((double) level * settings.mistcloakBlocks());
      case WARDING -> Optional.of(level * settings.wardingPercent() * PERCENT);
      case THRIFT -> Optional.of(-level * settings.thriftPercent() * PERCENT);
      case THIRST, SUNMARKED, BEACON -> Optional.empty();
    };
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
