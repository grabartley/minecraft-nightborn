package com.grahambartley.nightborn.tools.structures.nbt;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Nbt {

  public static final int END = 0;
  public static final int BYTE = 1;
  public static final int SHORT = 2;
  public static final int INT = 3;
  public static final int LONG = 4;
  public static final int FLOAT = 5;
  public static final int DOUBLE = 6;
  public static final int BYTE_ARRAY = 7;
  public static final int STRING = 8;
  public static final int LIST = 9;
  public static final int COMPOUND = 10;
  public static final int INT_ARRAY = 11;
  public static final int LONG_ARRAY = 12;

  private Nbt() {}

  public static Map<String, Object> compound(final Object... keysAndValues) {
    if (keysAndValues.length % 2 != 0) {
      throw new IllegalArgumentException("A compound needs a value for every key");
    }
    final Map<String, Object> tag = new LinkedHashMap<>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      tag.put((String) keysAndValues[i], keysAndValues[i + 1]);
    }
    return tag;
  }

  public static byte flag(final boolean on) {
    return on ? (byte) 1 : (byte) 0;
  }

  public static int tagId(final Object value) {
    return switch (value) {
      case Map<?, ?> ignored -> COMPOUND;
      case NbtList ignored -> LIST;
      case String ignored -> STRING;
      case Double ignored -> DOUBLE;
      case Float ignored -> FLOAT;
      case Long ignored -> LONG;
      case Short ignored -> SHORT;
      case Byte ignored -> BYTE;
      case Boolean ignored -> BYTE;
      case Integer ignored -> INT;
      case byte[] ignored -> BYTE_ARRAY;
      case int[] ignored -> INT_ARRAY;
      case long[] ignored -> LONG_ARRAY;
      default -> throw new IllegalArgumentException("Not an NBT value: " + value);
    };
  }
}
