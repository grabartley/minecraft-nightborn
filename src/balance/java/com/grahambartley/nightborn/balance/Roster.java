package com.grahambartley.nightborn.balance;

import java.util.Locale;
import java.util.Optional;

public record Roster(Kind kind, Optional<String> id) {

  public enum Kind {
    VAMPIRE,
    VAMPIRE_LORD,
    HUNTER,
    VETERAN_HUNTER,
    GUILD_MASTER;

    boolean vampire() {
      return this == VAMPIRE || this == VAMPIRE_LORD;
    }
  }

  public static Roster of(final Kind kind) {
    return new Roster(kind, Optional.empty());
  }

  public static Roster of(final Kind kind, final String id) {
    return new Roster(kind, Optional.of(id));
  }

  public boolean vampire() {
    return kind.vampire();
  }

  public String label() {
    return id.orElse(kind == Kind.VAMPIRE || kind == Kind.HUNTER ? "mixed" : kind.name())
        .toLowerCase(Locale.ROOT);
  }
}
