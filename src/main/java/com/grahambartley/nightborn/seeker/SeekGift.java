package com.grahambartley.nightborn.seeker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SeekGift(
    int trackRange,
    int trackTicks,
    boolean bloodTrails,
    float senseRange,
    float listenRange,
    double alliedShare) {

  public static final Codec<SeekGift> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, 128)
                          .optionalFieldOf("track_range", 0)
                          .forGetter(SeekGift::trackRange),
                      Codec.intRange(0, 2400)
                          .optionalFieldOf("track_ticks", 0)
                          .forGetter(SeekGift::trackTicks),
                      Codec.BOOL
                          .optionalFieldOf("blood_trails", false)
                          .forGetter(SeekGift::bloodTrails),
                      Codec.floatRange(0.0F, 64.0F)
                          .optionalFieldOf("sense_range", 0.0F)
                          .forGetter(SeekGift::senseRange),
                      Codec.floatRange(0.0F, 64.0F)
                          .optionalFieldOf("listen_range", 0.0F)
                          .forGetter(SeekGift::listenRange),
                      Codec.doubleRange(0.0, 1.0)
                          .optionalFieldOf("allied_share", 0.0)
                          .forGetter(SeekGift::alliedShare))
                  .apply(instance, SeekGift::new));
}
