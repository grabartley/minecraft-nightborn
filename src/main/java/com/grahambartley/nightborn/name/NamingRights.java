package com.grahambartley.nightborn.name;

import java.util.Optional;
import java.util.UUID;

public final class NamingRights {

  private NamingRights() {}

  public static boolean mayName(
      final Optional<UUID> namedBy, final UUID sender, final boolean alive) {
    return alive && namedBy.filter(sender::equals).isPresent();
  }
}
