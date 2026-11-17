package com.grahambartley.nightborn.guild;

import java.util.function.Function;
import net.minecraft.util.Identifier;

public final class WornLook {

  private HunterRig rig;
  private String id;
  private Identifier texture;

  public Identifier texture(
      final HunterRig worn, final String lookId, final Function<String, Identifier> resolve) {
    if (worn != rig || !lookId.equals(id)) {
      rig = worn;
      id = lookId;
      texture = resolve.apply(lookId);
    }
    return texture;
  }
}
