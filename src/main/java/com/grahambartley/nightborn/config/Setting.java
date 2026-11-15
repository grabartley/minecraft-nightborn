package com.grahambartley.nightborn.config;

public sealed interface Setting<T> permits IntSetting, BoolSetting {

  String section();

  String key();

  T defaultValue();

  T get(NightbornConfig config);

  T normalise(T value);

  default String id() {
    return section() + "." + key();
  }

  default String translationKey() {
    return "nightborn.setting." + section() + "." + key();
  }

  default String tooltipKey() {
    return translationKey() + ".tooltip";
  }
}
