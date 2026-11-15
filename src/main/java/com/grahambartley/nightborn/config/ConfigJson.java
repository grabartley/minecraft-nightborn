package com.grahambartley.nightborn.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.List;

public final class ConfigJson {

  public static final ConfigFormat<NightbornConfig> FORMAT =
      ConfigFormat.of(NightbornConfig.DEFAULTS, ConfigJson::read, ConfigJson::write);

  private ConfigJson() {}

  public static ConfigFormat.Read<NightbornConfig> read(final JsonObject root) {
    final List<String> problems = new ArrayList<>();
    SettingValues values = SettingValues.EMPTY;
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      final JsonElement element = elementFor(root, setting);
      if (element != null) {
        values = readInto(values, setting, element, problems);
      }
    }
    return new ConfigFormat.Read<>(NightbornConfig.from(values), problems);
  }

  public static JsonObject write(final NightbornConfig config) {
    final JsonObject root = new JsonObject();
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      final JsonObject section = sectionOf(root, setting.section());
      switch (setting) {
        case IntSetting number -> section.addProperty(setting.key(), number.get(config));
        case BoolSetting flag -> section.addProperty(setting.key(), flag.get(config));
      }
    }
    return root;
  }

  private static JsonElement elementFor(final JsonObject root, final Setting<?> setting) {
    final JsonElement section = root.get(setting.section());
    if (section == null || !section.isJsonObject()) {
      return null;
    }
    return section.getAsJsonObject().get(setting.key());
  }

  private static SettingValues readInto(
      final SettingValues values,
      final Setting<?> setting,
      final JsonElement element,
      final List<String> problems) {
    return switch (setting) {
      case IntSetting number -> readInt(values, number, element, problems);
      case BoolSetting flag -> readBool(values, flag, element, problems);
    };
  }

  private static SettingValues readInt(
      final SettingValues values,
      final IntSetting setting,
      final JsonElement element,
      final List<String> problems) {
    if (!isNumber(element)) {
      problems.add(setting.id() + " is not a whole number, using " + setting.defaultValue());
      return values;
    }
    final double raw = element.getAsDouble();
    if (!Double.isFinite(raw) || raw != Math.rint(raw)) {
      problems.add(setting.id() + " is not a whole number, using " + setting.defaultValue());
      return values;
    }
    final long whole = (long) raw;
    if (!setting.contains(whole)) {
      final int clamped = whole < setting.min() ? setting.min() : setting.max();
      problems.add(
          setting.id()
              + "="
              + whole
              + " is outside "
              + setting.min()
              + ".."
              + setting.max()
              + ", clamped to "
              + clamped);
      return values.with(setting, clamped);
    }
    return values.with(setting, (int) whole);
  }

  private static SettingValues readBool(
      final SettingValues values,
      final BoolSetting setting,
      final JsonElement element,
      final List<String> problems) {
    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
      return values.with(setting, element.getAsBoolean());
    }
    problems.add(setting.id() + " is not true or false, using " + setting.defaultValue());
    return values;
  }

  private static boolean isNumber(final JsonElement element) {
    return element.isJsonPrimitive() && ((JsonPrimitive) element).isNumber();
  }

  private static JsonObject sectionOf(final JsonObject root, final String name) {
    if (!root.has(name)) {
      root.add(name, new JsonObject());
    }
    return root.getAsJsonObject(name);
  }
}
