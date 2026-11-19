package com.grahambartley.nightborn.seeker;

import java.util.UUID;

public record QuarryMark(UUID target, long until, double share, int colour) {

  public boolean lasts(final long now) {
    return now < until;
  }

  public boolean on(final UUID entity) {
    return target.equals(entity);
  }
}
