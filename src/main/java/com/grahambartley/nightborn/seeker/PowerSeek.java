package com.grahambartley.nightborn.seeker;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;

public record PowerSeek(
    Optional<ScenePower> readScene,
    Optional<MarkPower> markQuarry,
    Optional<SightPower> trueSight,
    Optional<RevealPower> revelation) {

  public static final PowerSeek NONE =
      new PowerSeek(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

  public static final MapCodec<PowerSeek> CODEC =
      RecordCodecBuilder.<PowerSeek>mapCodec(
              instance ->
                  instance
                      .group(
                          ScenePower.CODEC
                              .optionalFieldOf("read_scene")
                              .forGetter(PowerSeek::readScene),
                          MarkPower.CODEC
                              .optionalFieldOf("mark_quarry")
                              .forGetter(PowerSeek::markQuarry),
                          SightPower.CODEC
                              .optionalFieldOf("true_sight")
                              .forGetter(PowerSeek::trueSight),
                          RevealPower.CODEC
                              .optionalFieldOf("revelation")
                              .forGetter(PowerSeek::revelation))
                      .apply(instance, PowerSeek::new))
          .validate(PowerSeek::single);

  public int count() {
    return (int)
        Stream.of(readScene, markQuarry, trueSight, revelation).filter(Optional::isPresent).count();
  }

  private static DataResult<PowerSeek> single(final PowerSeek seek) {
    return seek.count() > 1
        ? DataResult.error(() -> "A power reads a scene, marks, sees or reveals, one only")
        : DataResult.success(seek);
  }
}
