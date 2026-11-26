package com.grahambartley.nightborn.sound;

import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class BossMusicEnd {

  private BossMusicEnd() {}

  public static boolean stops(
      @Nullable final Identifier playing,
      final Set<Identifier> bossTracks,
      final Optional<Identifier> held) {
    return playing != null && bossTracks.contains(playing) && held.isEmpty();
  }
}
