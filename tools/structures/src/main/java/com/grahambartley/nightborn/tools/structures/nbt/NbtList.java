package com.grahambartley.nightborn.tools.structures.nbt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record NbtList(int kind, List<Object> items) {

  public NbtList {
    items = List.copyOf(items);
  }

  public static NbtList of(final int kind, final List<?> items) {
    return new NbtList(kind, List.copyOf(items));
  }

  public static NbtList ints(final int... values) {
    return new NbtList(Nbt.INT, Arrays.stream(values).boxed().map(Object.class::cast).toList());
  }

  public static NbtList doubles(final double... values) {
    return new NbtList(Nbt.DOUBLE, Arrays.stream(values).boxed().map(Object.class::cast).toList());
  }

  public static NbtList strings(final List<String> values) {
    return new NbtList(Nbt.STRING, List.copyOf(values));
  }

  public static NbtList compounds(final List<? extends Map<String, Object>> values) {
    return new NbtList(Nbt.COMPOUND, List.copyOf(values));
  }
}
