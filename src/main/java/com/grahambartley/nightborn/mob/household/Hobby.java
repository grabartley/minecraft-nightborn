package com.grahambartley.nightborn.mob.household;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringIdentifiable;

public enum Hobby implements StringIdentifiable {
  SIT,
  READ,
  CONVERSE,
  STROLL,
  GRAMOPHONE;

  public static final Codec<Hobby> CODEC = StringIdentifiable.createCodec(Hobby::values);

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
