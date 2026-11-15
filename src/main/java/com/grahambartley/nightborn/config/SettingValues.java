package com.grahambartley.nightborn.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SettingValues {

  public static final SettingValues EMPTY = new SettingValues(Map.of());

  private final Map<Setting<?>, Object> values;

  private SettingValues(final Map<Setting<?>, Object> values) {
    this.values = Collections.unmodifiableMap(values);
  }

  public static SettingValues of(final NightbornConfig config) {
    final Map<Setting<?>, Object> read = new LinkedHashMap<>();
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      read.put(setting, setting.get(config));
    }
    return new SettingValues(read);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(final Setting<T> setting) {
    return setting.normalise((T) values.get(setting));
  }

  public int get(final IntSetting setting) {
    return setting.normalise((Integer) values.get(setting));
  }

  public boolean get(final BoolSetting setting) {
    return setting.normalise((Boolean) values.get(setting));
  }

  public <T> SettingValues with(final Setting<T> setting, final T value) {
    final Map<Setting<?>, Object> updated = new LinkedHashMap<>(values);
    updated.put(setting, setting.normalise(value));
    return new SettingValues(updated);
  }
}
