package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.guild.HunterKit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record Veteran(
    Identifier weapon,
    Optional<Identifier> crucifix,
    double healthScale,
    double armor,
    List<Identifier> attacks,
    Optional<Identifier> rank,
    float boltDamage) {

  public static final double MOST_SCALE = 16.0;
  public static final double MOST_ARMOR = 30.0;

  public static final Codec<Veteran> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("weapon").forGetter(Veteran::weapon),
                      Identifier.CODEC.optionalFieldOf("crucifix").forGetter(Veteran::crucifix),
                      Codec.doubleRange(1.0, MOST_SCALE)
                          .optionalFieldOf("health_scale", 1.0)
                          .forGetter(Veteran::healthScale),
                      Codec.doubleRange(0.0, MOST_ARMOR)
                          .optionalFieldOf("armor", 0.0)
                          .forGetter(Veteran::armor),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("attacks", List.of())
                          .forGetter(Veteran::attacks),
                      Identifier.CODEC.optionalFieldOf("rank").forGetter(Veteran::rank),
                      Codec.floatRange(0.0F, HunterKit.MOST_DAMAGE)
                          .optionalFieldOf("bolt_damage", 0.0F)
                          .forGetter(Veteran::boltDamage))
                  .apply(instance, Veteran::new));

  public Veteran {
    attacks = List.copyOf(attacks);
  }

  public HunterKit kit() {
    return new HunterKit(weapon, Optional.empty(), crucifix, false, 1, 0.0F);
  }

  public double health(final double hunterHealth) {
    return hunterHealth * healthScale;
  }
}
