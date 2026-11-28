package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import org.jetbrains.annotations.Nullable;

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

  public record Scoring(double score, @Nullable Situation.Member ally) {

    static final Scoring NONE = new Scoring(0.0, null);
  }

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

  public Aim aimed() {
    return Aim.byKey(aim).orElse(Aim.TARGET);
  }

  public boolean helpsAnAlly() {
    return considerations.stream().anyMatch(Consideration::readsAlly);
  }

  public double score(final Situation situation) {
    return scoring(situation).score();
  }

  public Scoring scoring(final Situation situation) {
    if (!intent.equals(situation.intent().key()) || Aim.byKey(aim).isEmpty()) {
      return Scoring.NONE;
    }
    if (!helpsAnAlly()) {
      return new Scoring(weighed(situation), null);
    }
    Scoring best = Scoring.NONE;
    for (final Situation.Member ally : situation.group().helpable()) {
      final double score = weighed(situation.helping(ally));
      if (score > best.score()) {
        best = new Scoring(score, ally);
      }
    }
    return best;
  }

  private double weighed(final Situation situation) {
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
