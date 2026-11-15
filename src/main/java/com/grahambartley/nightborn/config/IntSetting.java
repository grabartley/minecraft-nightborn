package com.grahambartley.nightborn.config;

import java.util.function.ToIntFunction;

public record IntSetting(
    String section,
    String key,
    Integer defaultValue,
    int min,
    int max,
    ToIntFunction<NightbornConfig> reader)
    implements Setting<Integer> {

  public IntSetting {
    if (min > max || defaultValue < min || defaultValue > max) {
      throw new IllegalArgumentException(
          section + "." + key + " default " + defaultValue + " is not within " + min + ".." + max);
    }
  }

  public int clamp(final int value) {
    return Math.max(min, Math.min(max, value));
  }

  public boolean contains(final long value) {
    return value >= min && value <= max;
  }

  @Override
  public Integer get(final NightbornConfig config) {
    return reader.applyAsInt(config);
  }

  @Override
  public Integer normalise(final Integer value) {
    return value == null ? defaultValue : clamp(value);
  }
}
