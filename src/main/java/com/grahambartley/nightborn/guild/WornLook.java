package com.grahambartley.nightborn.guild;

import java.util.function.Function;
import net.minecraft.util.Identifier;

public final class WornLook {

  private String id;
  private Identifier texture;

  public Identifier texture(final String lookId, final Function<String, Identifier> resolve) {
    if (!lookId.equals(id)) {
      id = lookId;
      texture = resolve.apply(lookId);
    }
    return texture;
  }
}
