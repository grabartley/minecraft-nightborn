package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class FeedingSession {

  static final int RELEASE_TICKS = 8;
  static final int DRINK_INTERVAL_TICKS = 20;

  static final long NOT_A_FINISHER = -1L;

  private final UUID victim;
  private final BloodQuality quality;
  private final Optional<Identifier> gift;
  private final long finisherEndsAt;
  private final Set<UUID> alertedWitnesses = new HashSet<>();
  private Vec3d anchor;
  private Vec3d biterAnchor;
  private long lastHeldTick;
  private long nextDrinkTick;
  private boolean atLastHealth;
  private boolean turnRolled;
  private float pendingBlood;
  private float heldForBottles;
  private float totalTaken;
  private boolean seen;
  private boolean giftTaken;

  FeedingSession(
      final UUID victim,
      final BloodQuality quality,
      final Optional<Identifier> gift,
      final long now,
      final long finisherEndsAt) {
    this.victim = victim;
    this.quality = quality;
    this.gift = gift;
    this.lastHeldTick = now;
    this.nextDrinkTick = now + DRINK_INTERVAL_TICKS;
    this.finisherEndsAt = finisherEndsAt;
  }

  static FeedingSession bite(
      final UUID victim,
      final BloodQuality quality,
      final Optional<Identifier> gift,
      final long now) {
    return new FeedingSession(victim, quality, gift, now, NOT_A_FINISHER);
  }

  static FeedingSession finisher(
      final UUID victim,
      final BloodQuality quality,
      final Optional<Identifier> gift,
      final long now,
      final int drainTicks) {
    return new FeedingSession(victim, quality, gift, now, now + Math.max(1, drainTicks));
  }

  public boolean finisher() {
    return finisherEndsAt != NOT_A_FINISHER;
  }

  boolean finisherDue(final long now) {
    return finisher() && now >= finisherEndsAt;
  }

  Optional<Identifier> gift() {
    return gift;
  }

  Vec3d biterAnchorAt(final Vec3d position) {
    if (biterAnchor == null) {
      biterAnchor = position;
    }
    return biterAnchor;
  }

  public UUID victim() {
    return victim;
  }

  public BloodQuality quality() {
    return quality;
  }

  Vec3d anchorAt(final Vec3d position) {
    if (anchor == null) {
      anchor = position;
    }
    return anchor;
  }

  void held(final long now) {
    lastHeldTick = now;
  }

  boolean released(final long now) {
    return !finisher() && now - lastHeldTick > RELEASE_TICKS;
  }

  boolean drinkDue(final long now) {
    if (finisher() || now < nextDrinkTick) {
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

  void markSeen() {
    seen = true;
  }

  boolean seen() {
    return seen;
  }

  void markGiftTaken() {
    giftTaken = true;
  }

  boolean giftTaken() {
    return giftTaken;
  }
}
