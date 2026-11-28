package com.grahambartley.nightborn.mob.brain;

import java.util.Optional;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class FormHold {

  private long until = Long.MIN_VALUE;
  @Nullable private Vec3d course;

  public void take(final long now, final int holdTicks, @Nullable final Vec3d course) {
    until = now + holdTicks;
    this.course = course;
  }

  public boolean holds(final long now) {
    return now < until;
  }

  public Optional<Vec3d> course() {
    return Optional.ofNullable(course);
  }

  public void arrive() {
    course = null;
  }
}
