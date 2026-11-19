package com.grahambartley.nightborn.seeker;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record Scene(
    UUID vampire,
    Optional<Identifier> stage,
    List<Identifier> lines,
    Vec3d where,
    long at,
    boolean known) {

  public static Scene far(final UUID vampire, final Vec3d where, final long at) {
    return new Scene(vampire, Optional.empty(), List.of(), where, at, false);
  }

  public Scene {
    lines = List.copyOf(lines);
  }

  public boolean fresh(final long now, final int freshTicks) {
    return now - at <= freshTicks;
  }
}
