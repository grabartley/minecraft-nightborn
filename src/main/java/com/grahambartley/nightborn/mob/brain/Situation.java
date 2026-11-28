package com.grahambartley.nightborn.mob.brain;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public record Situation(
    Intent intent,
    Self self,
    @Nullable Target target,
    Place place,
    Group group,
    @Nullable Member ally) {

  public Situation(final Intent intent, final Self self, @Nullable final Target target) {
    this(intent, self, target, Place.UNKNOWN);
  }

  public Situation(
      final Intent intent, final Self self, @Nullable final Target target, final Place place) {
    this(intent, self, target, place, Group.UNKNOWN, null);
  }

  public Situation helping(final Member member) {
    return new Situation(intent, self, target, place, group, member);
  }

  public record Self(
      float health, int blood, boolean inForm, boolean sunlit, boolean downed, boolean stuck) {

    public static Self of(final float health, final int blood) {
      return new Self(health, blood, false, false, false, false);
    }
  }

  public record Target(
      double distance,
      boolean inSight,
      float health,
      boolean windingUp,
      boolean running,
      boolean slowed,
      boolean airborne,
      boolean vampire,
      boolean hunter,
      boolean aware) {

    public Target(final double distance, final boolean inSight, final float health) {
      this(distance, inSight, health, false, false, false, false, false, false);
    }

    public Target(
        final double distance,
        final boolean inSight,
        final float health,
        final boolean windingUp,
        final boolean running,
        final boolean slowed,
        final boolean airborne,
        final boolean vampire,
        final boolean hunter) {
      this(distance, inSight, health, windingUp, running, slowed, airborne, vampire, hunter, true);
    }
  }

  public record Place(
      double spotDistance,
      Route route,
      double homeDistance,
      double untilDawn,
      double light,
      double roomToFly) {

    public static final Place UNKNOWN = new Place(Double.NaN);

    public Place(final double spotDistance) {
      this(spotDistance, Route.UNKNOWN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }
  }

  public record Member(
      UUID id,
      double distance,
      float health,
      boolean underAttack,
      boolean healing,
      boolean fighting,
      boolean claimed) {}

  public record Group(boolean known, List<Member> allies, List<Double> foeDistances) {

    public static final Group UNKNOWN = new Group(false, List.of(), List.of());

    public Group {
      allies = List.copyOf(allies);
      foeDistances = List.copyOf(foeDistances);
    }

    public static Group of(final List<Member> allies, final List<Double> foeDistances) {
      return new Group(true, allies, foeDistances);
    }

    public double alliesWithin(final double radius) {
      return known
          ? allies.stream().filter(ally -> ally.fighting() && ally.distance() <= radius).count()
          : Double.NaN;
    }

    public double foesWithin(final double radius) {
      return known ? foeDistances.stream().filter(foe -> foe <= radius).count() : Double.NaN;
    }

    public boolean allyFighting() {
      return allies.stream().anyMatch(Member::fighting);
    }

    public List<Member> helpable() {
      return allies.stream().filter(ally -> !ally.claimed()).toList();
    }
  }
}
