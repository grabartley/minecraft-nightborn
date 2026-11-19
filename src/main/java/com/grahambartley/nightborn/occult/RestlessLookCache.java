package com.grahambartley.nightborn.occult;

import java.util.Optional;
import java.util.function.Function;

public final class RestlessLookCache {

  private String id;
  private Optional<RestlessLook> look = Optional.empty();

  public Optional<RestlessLook> resolve(
      final String lookId, final Function<String, Optional<RestlessLook>> find) {
    if (!lookId.equals(id)) {
      id = lookId;
      look = find.apply(lookId);
    }
    return look;
  }
}
