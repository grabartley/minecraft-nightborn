package com.grahambartley.nightborn.tools.structures.template;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BlockState {

  public static final String MC = "minecraft:";
  public static final String AIR = MC + "air";

  private static final Pattern STATE = Pattern.compile("([^\\[]+)(?:\\[(.*)\\])?$");

  private BlockState() {}

  public record Parsed(String block, Map<String, String> properties) {}

  public static String of(final String block, final Object... keysAndValues) {
    if (keysAndValues.length % 2 != 0) {
      throw new IllegalArgumentException("A block state needs a value for every property");
    }
    final Map<String, String> properties = new LinkedHashMap<>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      properties.put((String) keysAndValues[i], text(keysAndValues[i + 1]));
    }
    return of(block, properties);
  }

  public static String of(final String block, final Map<String, String> properties) {
    final String name = block.contains(":") ? block : MC + block;
    if (properties.isEmpty()) {
      return name;
    }
    final StringBuilder state = new StringBuilder(name).append('[');
    boolean first = true;
    for (final Map.Entry<String, String> property : new TreeMap<>(properties).entrySet()) {
      if (!first) {
        state.append(',');
      }
      first = false;
      state
          .append(property.getKey())
          .append('=')
          .append(property.getValue().toLowerCase(Locale.ROOT));
    }
    return state.append(']').toString();
  }

  public static Parsed parse(final String state) {
    final Matcher match = STATE.matcher(state);
    if (!match.matches()) {
      throw new IllegalArgumentException("Not a block state: " + state);
    }
    final Map<String, String> properties = new LinkedHashMap<>();
    if (match.group(2) != null && !match.group(2).isEmpty()) {
      for (final String property : match.group(2).split(",")) {
        final String[] pair = property.split("=");
        properties.put(pair[0], pair[1]);
      }
    }
    return new Parsed(match.group(1), properties);
  }

  public static String withProps(final String state, final Object... keysAndValues) {
    final Parsed parsed = parse(state);
    final Map<String, String> properties = new LinkedHashMap<>(parsed.properties());
    for (int i = 0; i < keysAndValues.length; i += 2) {
      properties.put(
          (String) keysAndValues[i], text(keysAndValues[i + 1]).toLowerCase(Locale.ROOT));
    }
    return of(parsed.block(), properties);
  }

  public static String full(final String state) {
    return state.contains(":") ? state : MC + state;
  }

  private static String text(final Object value) {
    return String.valueOf(value).toLowerCase(Locale.ROOT);
  }
}
