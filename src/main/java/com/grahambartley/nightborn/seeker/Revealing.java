package com.grahambartley.nightborn.seeker;

import java.util.Set;
import java.util.UUID;
import net.minecraft.util.Identifier;

public record Revealing(Set<UUID> vampires, long until, RevealPower power) {

  public Revealing {
    vampires = Set.copyOf(vampires);
  }

  public boolean lasts(final long now) {
    return now < until;
  }

  public boolean reveals(final UUID vampire) {
    return vampires.contains(vampire);
  }

  public boolean bars(final UUID vampire, final Identifier power) {
    return reveals(vampire) && this.power.bars().contains(power);
  }

  public boolean sharedWith(
      final boolean hunter, final boolean human, final double distanceSquared) {
    return hunter || human && distanceSquared <= (double) power.shareRange() * power.shareRange();
  }
}
