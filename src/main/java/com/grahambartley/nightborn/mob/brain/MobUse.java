package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record MobUse(String intent, String aim, List<Consideration> considerations, float weight) {

  public static final float MAX_WEIGHT = 100.0F;

  public static final Codec<MobUse> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.fieldOf("intent").forGetter(MobUse::intent),
                      Codec.STRING.optionalFieldOf("aim", Aim.TARGET.key()).forGetter(MobUse::aim),
                      Consideration.CODEC
                          .listOf()
                          .optionalFieldOf("considerations", List.of())
                          .forGetter(MobUse::considerations),
                      Codec.floatRange(0.0F, MAX_WEIGHT)
                          .optionalFieldOf("weight", 1.0F)
                          .forGetter(MobUse::weight))
                  .apply(instance, MobUse::new));

  public MobUse {
    considerations = List.copyOf(considerations);
  }

  public static MobUse inRange(final Intent intent, final float minRange, final float maxRange) {
    return new MobUse(
        intent.key(),
        Aim.TARGET.key(),
        List.of(new Consideration(Fact.TARGET_DISTANCE.key(), Curve.BETWEEN, minRange, maxRange)),
        1.0F);
  }

  public double score(final Situation situation) {
    if (!intent.equals(situation.intent().key()) || Aim.byKey(aim).isEmpty()) {
      return 0.0;
    }
    double score = weight;
    for (final Consideration consideration : considerations) {
      score *= consideration.value(situation);
      if (score <= 0.0) {
        return 0.0;
      }
    }
    return score;
  }
}
