package com.grahambartley.nightborn.coffin;

public record RestTally(int resting, int needed) {

  public boolean announces(final RestTally previous) {
    return this.resting > 0 && !this.equals(previous);
  }
}
