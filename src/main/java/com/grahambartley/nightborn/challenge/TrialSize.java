package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringIdentifiable;

public enum TrialSize implements StringIdentifiable {
  SMALL(0.25F),
  MEDIUM(0.5F),
  LARGE(1.0F);

  public static final Codec<TrialSize> CODEC = StringIdentifiable.createCodec(TrialSize::values);

  private final float share;

  TrialSize(final float share) {
    this.share = share;
  }

  public float share() {
    return share;
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
