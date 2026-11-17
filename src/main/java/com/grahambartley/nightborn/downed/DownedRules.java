package com.grahambartley.nightborn.downed;

import com.grahambartley.nightborn.config.DownedSettings;
import com.grahambartley.nightborn.downed.DownedState.Phase;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public final class DownedRules {

  private DownedRules() {}

  public enum Change {
    NONE,
    STOOD_UP,
    ESCAPED
  }

  public record Step(@Nullable DownedState state, Change change) {}

  public static boolean allowsDamage(
      @Nullable final DownedState state, final boolean killsVampires) {
    return state == null || killsVampires;
  }

  public static boolean downsInsteadOfDying(final boolean killsVampires) {
    return !killsVampires;
  }

  public static boolean helpless(@Nullable final DownedState state) {
    return state != null && state.isDowned();
  }

  public static boolean spares(
      final boolean targetHelpless, final boolean attackerIsMob, final boolean attackerHunts) {
    return targetHelpless && attackerIsMob && !attackerHunts;
  }

  public static boolean flees(@Nullable final DownedState state) {
    return state != null && !state.isDowned();
  }

  public static boolean stakesOnHit(@Nullable final DownedState state, final boolean stakeBlow) {
    return stakeBlow && state != null && state.isDowned();
  }

  public static Optional<UUID> hunter(final boolean human, final UUID attacker) {
    return human ? Optional.of(attacker) : Optional.empty();
  }

  public static DownedState downed(
      final DownedSettings settings, final Optional<UUID> hunter, final double timeFactor) {
    return new DownedState(
        Phase.DOWNED,
        (int) Math.max(1L, Math.round(settings.downedSeconds() * 20 * timeFactor)),
        hunter);
  }

  public static Step tick(final DownedState state, final DownedSettings settings) {
    if (state.ticksLeft() > 1) {
      return new Step(state.next(state.phase(), state.ticksLeft() - 1), Change.NONE);
    }
    if (state.phase() == Phase.DOWNED) {
      return new Step(state.next(Phase.ESCAPING, settings.escapeSeconds() * 20), Change.STOOD_UP);
    }
    return new Step(null, Change.ESCAPED);
  }

  public static float recoveredHealth(
      final float maxHealth, final DownedSettings settings, final double riseShare) {
    return Math.max(
        1.0F, maxHealth * (float) Math.max(settings.recoverHealth() / 100.0, riseShare));
  }
}
