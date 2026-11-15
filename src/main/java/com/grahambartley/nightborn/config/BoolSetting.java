package com.grahambartley.nightborn.config;

import java.util.function.Predicate;

public record BoolSetting(
    String section, String key, Boolean defaultValue, Predicate<NightbornConfig> reader)
    implements Setting<Boolean> {

  @Override
  public Boolean get(final NightbornConfig config) {
    return reader.test(config);
  }

  @Override
  public Boolean normalise(final Boolean value) {
    return value == null ? defaultValue : value;
  }
}
