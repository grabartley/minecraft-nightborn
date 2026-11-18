package com.grahambartley.nightborn.hunt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public record NotorietyKill(TagKey<EntityType<?>> victims, double amount, double lastOfBand) {

  public static final Codec<NotorietyKill> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE)
                          .fieldOf("victims")
                          .forGetter(NotorietyKill::victims),
                      Codec.doubleRange(0.0, Double.MAX_VALUE)
                          .fieldOf("amount")
                          .forGetter(NotorietyKill::amount),
                      Codec.doubleRange(0.0, Double.MAX_VALUE)
                          .optionalFieldOf("last_of_band", 0.0)
                          .forGetter(NotorietyKill::lastOfBand))
                  .apply(instance, NotorietyKill::new));

  public double worth(final boolean last) {
    return amount + (last ? lastOfBand : 0.0);
  }

  public static double worth(
      final Collection<NotorietyKill> kills,
      final Predicate<TagKey<EntityType<?>>> listed,
      final boolean last,
      final double stageMultiplier) {
    final double base =
        kills.stream()
            .filter(kill -> listed.test(kill.victims()))
            .mapToDouble(kill -> kill.worth(last))
            .max()
            .orElse(0.0);
    return base * Math.max(0.0, stageMultiplier);
  }
}
