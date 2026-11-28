package com.grahambartley.nightborn.mob.brain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Claims {

  private record Claim(UUID claimer, long until) {}

  private final Map<UUID, Claim> claims = new HashMap<>();

  public void claim(final UUID ally, final UUID claimer, final long until, final long now) {
    claims.values().removeIf(claim -> now >= claim.until());
    claims.put(ally, new Claim(claimer, until));
  }

  public boolean claimedByOther(final UUID ally, final UUID asker, final long now) {
    final Claim claim = claims.get(ally);
    if (claim == null) {
      return false;
    }
    if (now >= claim.until()) {
      claims.remove(ally);
      return false;
    }
    return !claim.claimer().equals(asker);
  }

  public int size() {
    return claims.size();
  }

  public void clear() {
    claims.clear();
  }
}
