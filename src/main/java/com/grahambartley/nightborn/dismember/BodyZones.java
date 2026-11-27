package com.grahambartley.nightborn.dismember;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public record BodyZones(double head, double hip, double trunk, Map<BodyPart, Integer> mobWeights) {

  public static final BodyZones DEFAULT =
      new BodyZones(
          0.25,
          0.375,
          0.5,
          Map.of(
              BodyPart.HEAD, 1,
              BodyPart.RIGHT_ARM, 5,
              BodyPart.LEFT_ARM, 5,
              BodyPart.RIGHT_LEG, 4,
              BodyPart.LEFT_LEG, 4));

  public static final Codec<BodyZones> CODEC =
      RecordCodecBuilder.<BodyZones>create(
              instance ->
                  instance
                      .group(
                          Codec.doubleRange(0.0, 1.0).fieldOf("head").forGetter(BodyZones::head),
                          Codec.doubleRange(0.0, 1.0).fieldOf("hip").forGetter(BodyZones::hip),
                          Codec.doubleRange(0.0, 1.0).fieldOf("trunk").forGetter(BodyZones::trunk),
                          Codec.unboundedMap(BodyPart.CODEC, Codec.intRange(0, 1000))
                              .fieldOf("mob_weights")
                              .forGetter(BodyZones::mobWeights))
                      .apply(instance, BodyZones::new))
          .validate(BodyZones::check);

  static DataResult<BodyZones> check(final BodyZones zones) {
    if (zones.head + zones.hip >= 1.0) {
      return DataResult.error(
          () ->
              "head "
                  + zones.head
                  + " and hip "
                  + zones.hip
                  + " leave no room for the trunk: they must add up to less than 1");
    }
    if (BodyPart.limbs().stream().mapToInt(zones::weight).sum() <= 0) {
      return DataResult.error(() -> "mob_weights must give at least one limb a weight above 0");
    }
    return DataResult.success(zones);
  }

  public int weight(final BodyPart part) {
    return mobWeights.getOrDefault(part, 0);
  }

  public Optional<BodyPart> at(final double height, final double side) {
    if (height >= 1.0 - head) {
      return Optional.of(BodyPart.HEAD);
    }
    final boolean right = side >= 0.0;
    if (height < hip) {
      return Optional.of(BodyPart.leg(right));
    }
    return Math.abs(side) > trunk ? Optional.of(BodyPart.arm(right)) : Optional.empty();
  }

  public BodyPart weighted(final double roll) {
    final int total = Arrays.stream(BodyPart.values()).mapToInt(this::weight).sum();
    double left = Math.clamp(roll, 0.0, 0.999999) * total;
    for (final BodyPart part : BodyPart.values()) {
      left -= weight(part);
      if (left < 0.0) {
        return part;
      }
    }
    return BodyPart.RIGHT_ARM;
  }
}
