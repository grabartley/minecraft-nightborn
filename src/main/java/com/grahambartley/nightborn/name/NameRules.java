package com.grahambartley.nightborn.name;

import java.util.Optional;
import net.minecraft.util.Formatting;

public final class NameRules {

  public static final int MAX_LENGTH = 32;
  public static final int MAX_SENT_LENGTH = 256;

  public enum Problem {
    EMPTY,
    TOO_LONG;

    public String translationKey() {
      return "nightborn.naming.problem." + name().toLowerCase(java.util.Locale.ROOT);
    }
  }

  public record Verdict(Optional<String> name, Optional<Problem> problem) {

    public boolean ok() {
      return name.isPresent();
    }
  }

  private NameRules() {}

  public static Verdict check(final String typed) {
    final String plain = Formatting.strip(typed);
    final StringBuilder kept = new StringBuilder();
    plain.codePoints().filter(NameRules::allowed).forEach(kept::appendCodePoint);
    final String name = kept.toString().strip();
    if (name.isEmpty()) {
      return new Verdict(Optional.empty(), Optional.of(Problem.EMPTY));
    }
    if (name.codePointCount(0, name.length()) > MAX_LENGTH) {
      return new Verdict(Optional.empty(), Optional.of(Problem.TOO_LONG));
    }
    return new Verdict(Optional.of(name), Optional.empty());
  }

  private static boolean allowed(final int codePoint) {
    final int type = Character.getType(codePoint);
    return codePoint != Formatting.FORMATTING_CODE_PREFIX
        && type != Character.CONTROL
        && type != Character.FORMAT
        && type != Character.PRIVATE_USE
        && type != Character.SURROGATE
        && type != Character.UNASSIGNED;
  }
}
