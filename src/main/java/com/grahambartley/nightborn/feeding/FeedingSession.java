package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FeedingSession {

  static final int RELEASE_TICKS = 8;
  static final int DRINK_INTERVAL_TICKS = 20;

  private final UUID victim;
  private final BloodQuality quality;
  private final Set<UUID> alertedWitnesses = new HashSet<>();
  private long lastHeldTick;
  private long nextDrinkTick;
  private boolean atLastHealth;
  private boolean bellRung;
  private boolean turnRolled;
  private float pendingBlood;
  private float heldForBottles;
  private float totalTaken;

  FeedingSession(final UUID victim, final BloodQuality quality, final long now) {
    this.victim = victim;
    this.quality = quality;
    this.lastHeldTick = now;
    this.nextDrinkTick = now + DRINK_INTERVAL_TICKS;
  }

  public UUID victim() {
    return victim;
  }

  public BloodQuality quality() {
    return quality;
  }

  void held(final long now) {
    lastHeldTick = now;
  }

  boolean released(final long now) {
    return now - lastHeldTick > RELEASE_TICKS;
  }

  boolean drinkDue(final long now) {
    if (now < nextDrinkTick) {
      return false;
    }
    nextDrinkTick = now + DRINK_INTERVAL_TICKS;
    return true;
  }

  boolean atLastHealth() {
    return atLastHealth;
  }

  void atLastHealth(final boolean value) {
    atLastHealth = value;
  }

  boolean firstSighting(final UUID witness) {
    return alertedWitnesses.add(witness);
  }

  boolean bellRung() {
    return bellRung;
  }

  void markBellRung() {
    bellRung = true;
  }

  boolean rollTurnOnce() {
    if (turnRolled) {
      return false;
    }
    turnRolled = true;
    return true;
  }

  void took(final float taken) {
    totalTaken += taken;
  }

  float totalTaken() {
    return totalTaken;
  }

  float pendingBlood() {
    return pendingBlood;
  }

  void pendingBlood(final float value) {
    pendingBlood = value;
  }

  float heldForBottles() {
    return heldForBottles;
  }

  void heldForBottles(final float value) {
    heldForBottles = value;
  }
}
