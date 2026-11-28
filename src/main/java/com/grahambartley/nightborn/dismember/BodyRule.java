package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.fight.Cutting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BodyRule(
    boolean maimable, boolean onlyWhenStaggered, float chanceScale, Cutting cutting) {

  public static final BodyRule DEFAULT = new BodyRule(false, false, 1.0F, Cutting.WEAPON);

  public static final Codec<BodyRule> CODEC =
      RecordCodecBuilder.<BodyRule>create(
              instance ->
                  instance
                      .group(
                          Codec.BOOL.fieldOf("maimable").forGetter(BodyRule::maimable),
                          Codec.BOOL
                              .optionalFieldOf("only_when_staggered", false)
                              .forGetter(BodyRule::onlyWhenStaggered),
                          Codec.floatRange(0.0F, 1.0F)
                              .optionalFieldOf("chance_scale", 1.0F)
                              .forGetter(BodyRule::chanceScale),
                          Cutting.CODEC
                              .optionalFieldOf("cutting", Cutting.WEAPON)
                              .forGetter(BodyRule::cutting))
                      .apply(instance, BodyRule::new))
          .validate(BodyRule::check);

  static DataResult<BodyRule> check(final BodyRule rule) {
    if (!rule.maimable && (rule.onlyWhenStaggered || rule.chanceScale != 1.0F)) {
      return DataResult.error(
          () ->
              "a body that cannot be maimed takes no only_when_staggered or chance_scale, got "
                  + rule);
    }
    return DataResult.success(rule);
  }
}
