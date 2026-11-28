package com.grahambartley.nightborn.mob.brain;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public enum Fact {
  TARGET_DISTANCE(situation -> target(situation, Situation.Target::distance)),
  TARGET_IN_SIGHT(situation -> target(situation, target -> flag(target.inSight()))),
  TARGET_HEALTH(situation -> target(situation, Situation.Target::health)),
  TARGET_WINDING_UP(situation -> target(situation, target -> flag(target.windingUp()))),
  TARGET_RUNNING(situation -> target(situation, target -> flag(target.running()))),
  TARGET_SLOWED(situation -> target(situation, target -> flag(target.slowed()))),
  TARGET_AIRBORNE(situation -> target(situation, target -> flag(target.airborne()))),
  TARGET_VAMPIRE(situation -> target(situation, target -> flag(target.vampire()))),
  TARGET_HUNTER(situation -> target(situation, target -> flag(target.hunter()))),
  OWN_HEALTH(situation -> situation.self().health()),
  OWN_BLOOD(situation -> situation.self().blood()),
  IN_FORM(situation -> flag(situation.self().inForm())),
  SUN_ON_ME(situation -> flag(situation.self().sunlit())),
  DOWNED(situation -> flag(situation.self().downed())),
  STUCK(situation -> flag(situation.self().stuck())),
  SPOT_DISTANCE(situation -> situation.place().spotDistance()),
  ALLY_DISTANCE(situation -> ally(situation, Situation.Member::distance)),
  ALLY_HEALTH(situation -> ally(situation, Situation.Member::health)),
  ALLY_UNDER_ATTACK(situation -> ally(situation, ally -> flag(ally.underAttack()))),
  ALLY_HEALING(situation -> ally(situation, ally -> flag(ally.healing()))),
  ALLIES_IN_RADIUS((situation, radius) -> situation.group().alliesWithin(radius)),
  FOES_IN_RADIUS((situation, radius) -> situation.group().foesWithin(radius));

  @FunctionalInterface
  private interface Reading {
    double read(Situation situation, double radius);
  }

  public static final Set<Fact> ALLY =
      Collections.unmodifiableSet(
          EnumSet.of(ALLY_DISTANCE, ALLY_HEALTH, ALLY_UNDER_ATTACK, ALLY_HEALING));

  public static final Set<Fact> RADIUS =
      Collections.unmodifiableSet(EnumSet.of(ALLIES_IN_RADIUS, FOES_IN_RADIUS));

  public static final Set<Fact> GROUP = union(ALLY, RADIUS);

  private static final Map<String, Fact> BY_KEY =
      Arrays.stream(values()).collect(Collectors.toMap(Fact::key, Function.identity()));

  private final Reading reading;

  Fact(final ToDoubleFunction<Situation> reading) {
    this((situation, radius) -> reading.applyAsDouble(situation));
  }

  Fact(final Reading reading) {
    this.reading = reading;
  }

  public double read(final Situation situation) {
    return read(situation, 0.0);
  }

  public double read(final Situation situation, final double radius) {
    return reading.read(situation, radius);
  }

  public String key() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<Fact> byKey(final String key) {
    return Optional.ofNullable(BY_KEY.get(key));
  }

  private static Set<Fact> union(final Set<Fact> first, final Set<Fact> second) {
    final Set<Fact> union = EnumSet.copyOf(first);
    union.addAll(second);
    return Collections.unmodifiableSet(union);
  }

  private static double target(
      final Situation situation, final ToDoubleFunction<Situation.Target> fact) {
    return situation.target() == null ? Double.NaN : fact.applyAsDouble(situation.target());
  }

  private static double ally(
      final Situation situation, final ToDoubleFunction<Situation.Member> fact) {
    return situation.ally() == null ? Double.NaN : fact.applyAsDouble(situation.ally());
  }

  private static double flag(final boolean on) {
    return on ? 1.0 : 0.0;
  }
}
