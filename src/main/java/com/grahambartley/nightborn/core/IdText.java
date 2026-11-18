package com.grahambartley.nightborn.core;

import java.util.Optional;
import net.minecraft.util.Identifier;

public final class IdText {

  private IdText() {}

  public static Optional<Identifier> parse(final String text) {
    return text.isEmpty() ? Optional.empty() : Optional.ofNullable(Identifier.tryParse(text));
  }

  public static String write(final Optional<Identifier> id) {
    return id.map(Identifier::toString).orElse("");
  }
}
