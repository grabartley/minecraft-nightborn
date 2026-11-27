package com.grahambartley.nightborn.mob.brain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
  SPOT_DISTANCE(situation -> situation.place().spotDistance());

  private static final Map<String, Fact> BY_KEY =
      Arrays.stream(values()).collect(Collectors.toMap(Fact::key, Function.identity()));

  private final ToDoubleFunction<Situation> reading;

  Fact(final ToDoubleFunction<Situation> reading) {
    this.reading = reading;
  }

  public double read(final Situation situation) {
    return reading.applyAsDouble(situation);
  }

  public String key() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<Fact> byKey(final String key) {
    return Optional.ofNullable(BY_KEY.get(key));
  }

  private static double target(
      final Situation situation, final ToDoubleFunction<Situation.Target> fact) {
    return situation.target() == null ? Double.NaN : fact.applyAsDouble(situation.target());
  }

  private static double flag(final boolean on) {
    return on ? 1.0 : 0.0;
  }
}
