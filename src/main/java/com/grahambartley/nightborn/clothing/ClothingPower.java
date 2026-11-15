package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.config.ClothingSettings;
import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.StringIdentifiable;

public enum ClothingPower implements StringIdentifiable {
  BATWING(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE),
  GLIDING_STEP(EntityAttributes.GENERIC_STEP_HEIGHT),
  HUSH(EntityAttributes.PLAYER_SNEAKING_SPEED);

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

  public double amount(final int level, final ClothingSettings settings) {
    return switch (this) {
      case BATWING -> (double) level * settings.batwingBlocks();
      case GLIDING_STEP -> settings.glidingStepTenths() * TENTH;
      case HUSH -> level * settings.hushSpeed() * PERCENT;
    };
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
