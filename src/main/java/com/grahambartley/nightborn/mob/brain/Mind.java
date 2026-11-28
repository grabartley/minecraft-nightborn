package com.grahambartley.nightborn.mob.brain;

import java.util.Set;

public record Mind(int thinkTicks, Sharpness sharpness, Disposition disposition) {

  public static Mind plain(final int thinkTicks) {
    return new Mind(thinkTicks, Sharpness.KEEN, Disposition.EVEN);
  }

  public Set<Fact> reads(final Set<Fact> needed) {
    return sharpness.visible(needed);
  }

  public boolean readsGroup() {
    return sharpness.reads(Sharpness.Reads.GROUP);
  }

  public IntentBudget budget(final IntentBudget base) {
    return base.withThrift(
        Math.clamp(base.thrift() * sharpness.foresight() * disposition.thrift(), 0.0F, 1.0F));
  }

  public double judged(final double score, final Intent intent, final Aim aim, final double roll) {
    return sharpness.blurred(score * disposition.lean(intent, aim), roll);
  }
}
