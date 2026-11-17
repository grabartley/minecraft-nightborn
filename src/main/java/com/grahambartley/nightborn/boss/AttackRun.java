package com.grahambartley.nightborn.boss;

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

  public int strikeTick(final long now) {
    return age(now) - attack.tellTicks();
  }
}
