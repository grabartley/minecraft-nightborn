package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.util.StringIdentifiable;

public enum Intent implements StringIdentifiable {
  FIGHT,
  RETREAT,
  CLOSE_IN,
  HUNT,
  TRAVEL,
  GUARD,
  REST;

  public static final Codec<Intent> CODEC = StringIdentifiable.createCodec(Intent::values);

  private static final Map<String, Intent> BY_KEY =
      Arrays.stream(values()).collect(Collectors.toMap(Intent::key, Function.identity()));

  public boolean fights() {
    return this == FIGHT || this == CLOSE_IN;
  }

  public String key() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<Intent> byKey(final String key) {
    return Optional.ofNullable(BY_KEY.get(key));
  }

  @Override
  public String asString() {
    return key();
  }
}
