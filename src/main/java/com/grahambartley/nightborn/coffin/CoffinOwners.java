package com.grahambartley.nightborn.coffin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CoffinOwners<A> {

  private final Map<UUID, A> coffins = new HashMap<>();
  private final Map<A, UUID> owners = new HashMap<>();

  public Optional<UUID> ownerOf(final A coffin) {
    return Optional.ofNullable(this.owners.get(coffin));
  }

  public Optional<A> coffinOf(final UUID owner) {
    return Optional.ofNullable(this.coffins.get(owner));
  }

  public boolean assign(final UUID owner, final A coffin) {
    if (this.owners.containsKey(coffin)) {
      return false;
    }
    this.release(owner);
    this.coffins.put(owner, coffin);
    this.owners.put(coffin, owner);
    return true;
  }

  public Optional<A> release(final UUID owner) {
    final A coffin = this.coffins.remove(owner);
    if (coffin != null) {
      this.owners.remove(coffin);
    }
    return Optional.ofNullable(coffin);
  }

  public Optional<UUID> releaseCoffin(final A coffin) {
    final UUID owner = this.owners.remove(coffin);
    if (owner != null) {
      this.coffins.remove(owner);
    }
    return Optional.ofNullable(owner);
  }

  public Map<UUID, A> entries() {
    return Collections.unmodifiableMap(this.coffins);
  }
}
