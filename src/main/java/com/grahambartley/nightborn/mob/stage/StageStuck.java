package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StageStuck(
    int ticks,
    double progress,
    double travel,
    double reach,
    int holdWaits,
    int stepBackWaits,
    int tries,
    int forgoWaits,
    int stepBackRange) {

  public static final StageStuck PLAIN = new StageStuck(40, 0.5, 2.0, 1.5, 3, 1, 3, 10, 6);

  public static final Codec<StageStuck> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 200)
                          .optionalFieldOf("ticks", PLAIN.ticks)
                          .forGetter(StageStuck::ticks),
                      Codec.doubleRange(0.05, 8.0)
                          .optionalFieldOf("progress", PLAIN.progress)
                          .forGetter(StageStuck::progress),
                      Codec.doubleRange(0.5, 16.0)
                          .optionalFieldOf("travel", PLAIN.travel)
                          .forGetter(StageStuck::travel),
                      Codec.doubleRange(0.0, 8.0)
                          .optionalFieldOf("reach", PLAIN.reach)
                          .forGetter(StageStuck::reach),
                      Codec.intRange(1, 20)
                          .optionalFieldOf("hold_waits", PLAIN.holdWaits)
                          .forGetter(StageStuck::holdWaits),
                      Codec.intRange(1, 20)
                          .optionalFieldOf("step_back_waits", PLAIN.stepBackWaits)
                          .forGetter(StageStuck::stepBackWaits),
                      Codec.intRange(1, 20)
                          .optionalFieldOf("tries", PLAIN.tries)
                          .forGetter(StageStuck::tries),
                      Codec.intRange(1, 100)
                          .optionalFieldOf("forgo_waits", PLAIN.forgoWaits)
                          .forGetter(StageStuck::forgoWaits),
                      Codec.intRange(1, 32)
                          .optionalFieldOf("step_back_range", PLAIN.stepBackRange)
                          .forGetter(StageStuck::stepBackRange))
                  .apply(instance, StageStuck::new));
}
