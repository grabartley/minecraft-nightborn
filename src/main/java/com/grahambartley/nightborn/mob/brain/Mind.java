package com.grahambartley.nightborn.mob.brain;

import java.util.Set;
import java.util.function.DoubleSupplier;

public record Mind(int thinkTicks, Sharpness sharpness, Disposition disposition) {

  public Set<Fact> reads(final Set<Fact> needed) {
    return sharpness.visible(needed);
  }

  public boolean readsGroup() {
    return sharpness.reads(Sharpness.Reads.GROUP);
  }

  public boolean chains() {
    return sharpness.chains();
  }

  public IntentBudget budget(final IntentBudget base) {
    return base.withThrift(
        Math.clamp(base.thrift() * sharpness.foresight() * disposition.thrift(), 0.0F, 1.0F));
  }

  public double judged(final double score, final Aim aim, final DoubleSupplier chance) {
    final double leaned = score * disposition.lean(aim);
    return sharpness.blurs() ? sharpness.blurred(leaned, chance.getAsDouble()) : leaned;
  }
}
