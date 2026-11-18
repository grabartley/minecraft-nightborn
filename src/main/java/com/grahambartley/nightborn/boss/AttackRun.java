package com.grahambartley.nightborn.boss;

import java.util.Optional;
import net.minecraft.util.Identifier;

public record AttackRun(Identifier id, BossAttack attack, long startedAt) {

  public enum Stage {
    TELL,
    STRIKE,
    DONE
  }

  public int age(final long now) {
    return (int) Math.max(0L, now - startedAt);
  }

  public Stage stage(final long now) {
    final int age = age(now);
    if (age < attack.tellTicks()) {
      return Stage.TELL;
    }
    return age <= attack.tellTicks() + attack.strikeTicks() ? Stage.STRIKE : Stage.DONE;
  }

  public boolean moves() {
    return attack.zone().map(zone -> zone.motion() != ZoneAttack.Motion.STAY).orElse(false);
  }

  public boolean drains(final long now) {
    return attack.drain().isPresent() && stage(now) == Stage.STRIKE;
  }

  public Optional<ChannelBar> channelBar(final long now) {
    return stage(now) == Stage.STRIKE
        ? attack.channel().flatMap(ChannelAttack::bar)
        : Optional.empty();
  }

  public Optional<Identifier> held(final long now) {
    return stage(now) == Stage.STRIKE
        ? attack.channel().flatMap(ChannelAttack::held)
        : Optional.empty();
  }

  public int strikeTick(final long now) {
    return age(now) - attack.tellTicks();
  }

  public static boolean holdsStrike(final int strikeTicks) {
    return strikeTicks > 0;
  }
}
