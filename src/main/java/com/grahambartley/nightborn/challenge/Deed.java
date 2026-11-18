package com.grahambartley.nightborn.challenge;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.Identifier;

public record Deed(Identifier kind, Map<String, String> facts, int amount) {

  public Deed {
    facts = Map.copyOf(facts);
    amount = Math.max(0, amount);
  }

  public static Deed of(final Identifier kind) {
    return new Deed(kind, Map.of(), 1);
  }

  public Deed with(final String key, final String value) {
    final Map<String, String> next = new HashMap<>(facts);
    next.put(key, value);
    return new Deed(kind, next, amount);
  }

  public Deed with(final String key, final boolean value) {
    return with(key, Boolean.toString(value));
  }

  public Deed with(final String key, final Identifier value) {
    return with(key, value.toString());
  }

  public Deed with(final String key, final int value) {
    return with(key, Integer.toString(value));
  }

  public Deed times(final int count) {
    return new Deed(kind, facts, count);
  }

  public Optional<String> fact(final String key) {
    return Optional.ofNullable(facts.get(key));
  }

  public OptionalInt number(final String key) {
    final String value = facts.get(key);
    if (value == null) {
      return OptionalInt.empty();
    }
    try {
      return OptionalInt.of(Integer.parseInt(value));
    } catch (final NumberFormatException notANumber) {
      return OptionalInt.empty();
    }
  }
}
