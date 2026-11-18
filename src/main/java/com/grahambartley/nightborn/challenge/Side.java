package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.StringIdentifiable;

public enum Side implements StringIdentifiable {
  VAMPIRE,
  HUNTER,
  ANYONE;

  public static final Codec<Side> CODEC = StringIdentifiable.createCodec(Side::values);

  public static Optional<Side> of(final boolean vampire, final boolean hunter) {
    if (vampire) {
      return Optional.of(VAMPIRE);
    }
    return hunter ? Optional.of(HUNTER) : Optional.empty();
  }

  public boolean holds(final Optional<Side> player) {
    return this == ANYONE || player.filter(this::equals).isPresent();
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
