package com.grahambartley.nightborn.journal;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public enum Journal implements StringIdentifiable {
  HUNTER,
  VAMPIRE;

  public static final Codec<Journal> CODEC = StringIdentifiable.createCodec(Journal::values);

  public static Optional<Journal> ofSide(final boolean vampire, final boolean hunter) {
    if (vampire) {
      return Optional.of(VAMPIRE);
    }
    return hunter ? Optional.of(HUNTER) : Optional.empty();
  }

  public Identifier id() {
    return Identifier.of(Nightborn.MOD_ID, asString() + "_journal");
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
