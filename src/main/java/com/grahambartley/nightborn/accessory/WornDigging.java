package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class WornDigging {

  public static final TagKey<Block> GRAVE_SOIL =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "grave_soil"));

  private WornDigging() {}

  public static float speed(final LivingEntity digger, final BlockState state, final float speed) {
    return speed(
        speed, state.isIn(GRAVE_SOIL), GiftAttributes.value(digger, GiftAttributes.SOIL_DIG_SPEED));
  }

  public static float speed(final float speed, final boolean soil, final double factor) {
    return soil ? (float) (speed * Math.max(1.0, factor)) : speed;
  }
}
